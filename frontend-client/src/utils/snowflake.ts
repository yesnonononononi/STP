class Snowflake {
  private epoch: bigint;
  private workerIdBits: number;
  private maxWorkerId: bigint;
  private sequenceBits: number;
  private workerIdShift: number;
  private timestampLeftShift: number;
  private sequenceMask: bigint;
  private workerId: bigint;
  private sequence: bigint;
  private lastTimestamp: bigint;

  constructor(workerId: number = 1, epoch: number = 1704067200000) { // 基准时间：2024-01-01T00:00:00Z
    this.epoch = BigInt(epoch);
    this.workerIdBits = 10; // 支持最大 1024 个节点
    this.maxWorkerId = BigInt(-1 ^ (-1 << this.workerIdBits));
    this.sequenceBits = 12; // 毫秒内最大序列 4096
    
    this.workerIdShift = this.sequenceBits;
    this.timestampLeftShift = this.sequenceBits + this.workerIdBits;
    this.sequenceMask = BigInt(-1 ^ (-1 << this.sequenceBits));
    
    this.workerId = BigInt(workerId);
    if (this.workerId > this.maxWorkerId || this.workerId < 0n) {
      throw new Error(`Worker ID must be between 0 and ${this.maxWorkerId}`);
    }
    
    this.sequence = 0n;
    this.lastTimestamp = -1n;
  }

  public nextId(): string {
    let timestamp = BigInt(Date.now());
    
    if (timestamp < this.lastTimestamp) {
      throw new Error("Clock moved backwards. Refusing to generate id.");
    }
    
    if (timestamp === this.lastTimestamp) {
      this.sequence = (this.sequence + 1n) & this.sequenceMask;
      if (this.sequence === 0n) {
        // 序列号溢出，等待下一毫秒
        while (timestamp <= this.lastTimestamp) {
          timestamp = BigInt(Date.now());
        }
      }
    } else {
      this.sequence = 0n;
    }
    
    this.lastTimestamp = timestamp;
    
    const id = ((timestamp - this.epoch) << BigInt(this.timestampLeftShift)) |
               (this.workerId << BigInt(this.workerIdShift)) |
               this.sequence;
               
    return id.toString();
  }
}

const snowflake = new Snowflake(1);

export function getSnowflakeId(): string {
  return snowflake.nextId();
}
