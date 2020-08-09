import { NativeModules } from 'react-native';

type DashPayType = {
  multiply(a: number, b: number): Promise<number>;
};

const { DashPay } = NativeModules;

export default DashPay as DashPayType;
