export interface Zone {
  id: string;
  code: string;
  name: string;
  locality: string;
}

export interface Address {
  id: string;
  zoneId: string;
  zoneName: string;
  street: string;
  streetNumber: string;
  neighborhood?: string;
  locality: string;
  references?: string;
  primary: boolean;
}

export interface CustomerSummary {
  id: string;
  firstName: string;
  lastName: string;
  phone: string;
  whatsapp?: string;
  email?: string;
  status: string;
  primaryAddress?: Address;
}

export interface CreateCustomerRequest {
  firstName: string;
  lastName: string;
  phone: string;
  whatsapp?: string;
  email?: string;
  acquisitionSource?: string;
  primaryAddress: {
    zoneId: string;
    street: string;
    streetNumber: string;
    neighborhood?: string;
    locality: string;
    references?: string;
  };
  preferences: {
    fragrance?: string;
    softenerAllowed: boolean;
    babyClothes: boolean;
    dryerAllowed: boolean;
    colorMixAllowed: boolean;
    exclusiveCycle: boolean;
    stainTreatment: boolean;
  };
}
