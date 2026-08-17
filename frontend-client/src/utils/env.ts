export enum EnvType {
  SHARED_WORKER = 'SHARED_WORKER',
  NORMAL = 'NORMAL',
}

export class EnvUtils {
  public static getEnvType(): EnvType {
    if (typeof window !== 'undefined' && typeof window.SharedWorker !== 'undefined') {
      return EnvType.SHARED_WORKER
    }
    return EnvType.NORMAL
  }
}
