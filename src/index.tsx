import { NativeModules } from 'react-native';

type MobileResults = {
  displayTest: string;
  responseCode: string;
  result: string;
}

type DashPayType = {
  multiply(a: number, b: number): Promise<number>;
  pay(REFERENCE_NUMBER: string,TRANSACTION_ID: string,OPERATOR_ID: string,ADDITIONAL_AMOUNT:string,AMOUNT: string,TRANSACTION_TYPE: string,EXTRA_ORIGINATING_URI:string): Promise<string>;
  getTransactionResults(): Promise<MobileResults>;
};

const { DashPay } = NativeModules;

export default DashPay as DashPayType;
