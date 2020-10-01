import { NativeModules } from 'react-native';

type DashPayType = {
  multiply(a: number, b: number): Promise<number>;
  pay(REFERENCE_NUMBER: string,TRANSACTION_ID: string,OPERATOR_ID: string,ADDITIONAL_AMOUNT:string,AMOUNT: string,TRANSACTION_TYPE: string,EXTRA_ORIGINATING_URI:string): Promise<string>;
  getTransactionResults(): Promise<string>;
  getImei(): Promise<string[]>;
  GetSerialNumber(): Promise<string>;
};

const { DashPay } = NativeModules;

export default DashPay as DashPayType;
