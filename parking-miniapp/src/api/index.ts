import { get, post } from '@/utils/request'
import type { Result, LoginVO, LotVO, VehicleVO, SubscriptionVO, WxPayParams, WxUser, PlanType } from '@/types'

// ========== 认证 ==========
export const authApi = {
  // 微信登录（code2session，首次自动注册）
  wxLogin: (code: string, nickname?: string, avatar?: string) =>
    post<Result<LoginVO>>('/api/auth/wx-login', { code, nickname, avatar }),
  // 当前用户资料
  profile: () => get<Result<WxUser>>('/api/my/profile')
}

// ========== 车场（扫码公开接口） ==========
export const lotApi = {
  // 扫码校验并返回车场信息
  scan: (lotId: number | string, sign: string) =>
    get<Result<LotVO>>('/api/lot/scan', { lotId, sign })
}

// ========== 车辆 ==========
export const vehicleApi = {
  // 扫码添加车辆（默认绑定扫码车场）
  add: (data: { plateNo: string; lotId: number | string; sign: string }) =>
    post<Result<VehicleVO>>('/api/vehicle/add', data),
  // 我的车辆
  my: () => get<Result<VehicleVO[]>>('/api/vehicle/my'),
  // 解绑
  unbind: (vehicleId: number) => post<Result<void>>('/api/vehicle/unbind', { vehicleId })
}

// ========== 订阅/续费 ==========
export const subscriptionApi = {
  // 续费下单：服务端计价，返回微信支付参数
  order: (vehicleId: number, planType: PlanType) =>
    post<Result<WxPayParams>>('/api/subscription/order', { vehicleId, planType }),
  // 模拟支付成功（后端 mock 开关开启时，packageVal 以 "mock:" 开头）
  mockNotify: (orderId: number) =>
    post<Result<void>>('/api/pay/mock/notify', { orderId }),
  // 我的订阅记录
  my: () => get<Result<SubscriptionVO[]>>('/api/subscription/my'),
  // 某车辆的订阅记录
  byVehicle: (vehicleId: number) =>
    get<Result<SubscriptionVO[]>>(`/api/subscription/vehicle/${vehicleId}`)
}
