#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
STP 批量注册用户分批带间隔注入脚本 (ES 性能与同步压测)
"""

import sys
import io
import time
import random
import requests
from concurrent.futures import ThreadPoolExecutor, as_completed

# 强制 stdout 输出 UTF-8 编码，防止 Windows 控制台编码崩溃
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

# 默认配置
DEFAULT_BASE_URL = "http://localhost:8081"  # 切换为 auth 8081 服务直连地址
DEFAULT_VERIFY_CODE = "123456"               # 默认验证码 (123456)
DEFAULT_PASSWORD = "Password123"             # 默认密码 (只能包含字母、数字、下划线)
TOTAL_COUNT = 50                             # 注册总用户数 (50条)
BATCH_SIZE = 10                              # 每批发送数量 (10条/批)
BATCH_INTERVAL = 2                         # 批次间休眠间隔 (0.5秒)
CONCURRENCY_PER_BATCH = 5                    # 每批内部并发线程数 (5线程)

def generate_phone_number(index: int) -> str:
  """根据当前微秒级时间戳与索引生成绝对不重复的合规 11 位手机号"""
  prefix = random.choice(["139", "158", "188", "199"])
  unique_num = (int(time.time() * 10000) + index) % 100000000
  return f"{prefix}{unique_num:08d}"

def register_user(session: requests.Session, base_url: str, phone: str, password: str, verify_code: str):
  """发送注册请求"""
  url = f"{base_url.rstrip('/')}/user-auth/register"
  payload = {
    "phoneNumber": phone,
    "password": password,
    "verifyCode": verify_code
  }
  headers = {
    "Content-Type": "application/json"
  }
  
  start_time = time.time()
  try:
    response = session.post(url, json=payload, headers=headers, timeout=5)
    elapsed = (time.time() - start_time) * 1000
    
    if response.status_code == 200:
      res_json = response.json()
      if res_json.get("code") == 1 or res_json.get("success") is True:
        return True, phone, elapsed, None
      else:
        err_msg = res_json.get("msg") or res_json.get("errMsg") or str(res_json)
        return False, phone, elapsed, f"BizError: {err_msg}"
    else:
      return False, phone, elapsed, f"HTTP {response.status_code}: {response.text[:100]}"
  except Exception as e:
    elapsed = (time.time() - start_time) * 1000
    return False, phone, elapsed, f"Exception: {str(e)}"

def run_batched_registration(
  total_count: int = TOTAL_COUNT,
  batch_size: int = BATCH_SIZE,
  interval: float = BATCH_INTERVAL,
  concurrency: int = CONCURRENCY_PER_BATCH,
  base_url: str = DEFAULT_BASE_URL
):
  print("=" * 68,flush=True)
  print("🚀 开始执行 STP 分批带间隔用户注册注入 (ES 性能与同步压测)",flush=True)
  print(f"📍 目标地址: {base_url}/user-auth/register",flush=True)
  print(f"🔑 默认验证码: {DEFAULT_VERIFY_CODE} | 密码: {DEFAULT_PASSWORD}",flush=True)
  print(f"👥 总目标注册数: {total_count} 条 | 每批数量: {batch_size} 条 | 批间隔: {interval}s | 批内并发: {concurrency}",flush=True)
  print("=" * 68,flush=True)

  # 起始随机基数防止手机号重复冲突
  base_index = random.randint(100000, 900000)
  all_phones = [generate_phone_number(base_index + i) for i in range(total_count)]

  session = requests.Session()
  adapter = requests.adapters.HTTPAdapter(pool_connections=concurrency * 2, pool_maxsize=concurrency * 4)
  session.mount("http://", adapter)
  session.mount("https://", adapter)

  total_success = 0
  total_fail = 0
  sum_latency = 0.0
  start_all_time = time.time()

  # 将总数据切分成批次 (Batches)
  batches = [all_phones[i:i + batch_size] for i in range(0, len(all_phones), batch_size)]
  total_batches = len(batches)

  for batch_idx, batch_phones in enumerate(batches, start=1):
    print(f"\n📦 [批次 {batch_idx}/{total_batches}] 开始注入 {len(batch_phones)} 条用户记录...",flush=True)
    batch_start = time.time()
    batch_success = 0
    batch_fail = 0

    with ThreadPoolExecutor(max_workers=min(concurrency, len(batch_phones))) as executor:
      futures = [
        executor.submit(register_user, session, base_url, phone, DEFAULT_PASSWORD, DEFAULT_VERIFY_CODE)
        for phone in batch_phones
      ]

      for future in as_completed(futures):
        success, phone, latency, err = future.result()
        sum_latency += latency

        if success:
          batch_success += 1
          total_success += 1
        else:
          batch_fail += 1
          total_fail += 1
          print(f"  ❌ 注册失败 ({phone}): {err}")

    batch_elapsed = time.time() - batch_start
    print(f"  ✅ [批次 {batch_idx} 完成] 成功: {batch_success}/{len(batch_phones)} | 本批耗时: {batch_elapsed:.2f}s",flush=True)

    # 如果不是最后一批，执行冷却休眠
    if batch_idx < total_batches:
      print(f"  ⏳ 缓冲休眠 {interval}s 避开微服务连接池与 Seata 锁峰值...")
      time.sleep(interval)

  total_elapsed = time.time() - start_all_time
  avg_latency = sum_latency / total_count if total_count > 0 else 0
  avg_qps = total_count / total_elapsed if total_elapsed > 0 else 0

  print("\n" + "=" * 68)
  print("📊 【分批带间隔注入 — 汇总报告】")
  print(f"⏱️ 总完成时间: {total_elapsed:.2f} 秒 (包含批次休眠时间)")
  print(f"✅ 成功注册入库: {total_success} / {total_count} ({total_success / total_count * 100:.1f}%)")
  print(f"❌ 失败请求数量: {total_fail} / {total_count}")
  print(f"🚀 平均吞吐速率: {avg_qps:.2f} Req/sec")
  print(f"⏱️ 接口平均响应延迟: {avg_latency:.2f} ms")
  print("=" * 68)

if __name__ == "__main__":
  url = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BASE_URL
  count = int(sys.argv[2]) if len(sys.argv) > 2 else TOTAL_COUNT
  batch = int(sys.argv[3]) if len(sys.argv) > 3 else BATCH_SIZE
  run_batched_registration(total_count=count, batch_size=batch, base_url=url)
