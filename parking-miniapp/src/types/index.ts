// 后端统一响应
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// 登录响应
export interface LoginVO {
  token: string
  userId: number
  role: string
  name: string
}

// 车场信息（扫码返回）
export interface LotVO {
  id: number
  name: string
  location: string
  totalSpaces: number
  bindLimit: number | null
  bindCount: number
  monthlyPrice: number | null
  quarterlyPrice: number | null
  yearlyPrice: number | null
  freeMinutes: number
  firstHours: number
  firstPrice: number
  hourlyPrice: number
  dailyMax: number | null
}

// 车辆
export interface VehicleVO {
  id: number
  plateNo: string
  lotId: number
  lotName: string
  isOwner: boolean | null
  latestExpire: string | null
  planType: string | null
  subStatus: 'VALID' | 'EXPIRED' | 'NONE'
  userIds: number[]
}

// 订阅记录
export interface SubscriptionVO {
  id: number
  vehicleId: number
  plateNo: string
  planType: 'MONTH' | 'QUARTER' | 'YEAR'
  price: number
  startTime: string
  expireTime: string
}

// 用户资料
export interface WxUser {
  id: number
  nickname: string
  avatar: string
  phone: string | null
}

// 微信支付参数（后端 PrepayWithRequestPaymentResponse）
export interface WxPayParams {
  appId: string
  timeStamp: string
  nonceStr: string
  packageVal: string
  signType: string
  paySign: string
}

export type PlanType = 'MONTH' | 'QUARTER' | 'YEAR'

export const PLAN_TEXT: Record<PlanType, string> = {
  MONTH: '包月',
  QUARTER: '包季',
  YEAR: '包年'
}
