export interface ClientPreferences {
  fragrance: string | null;
  softenerAllowed: boolean | null;
  dryerAllowed: boolean | null;
  hypoallergenic: boolean | null;
  separateColors: boolean | null;
  specialInstructions: string | null;
}

export interface ClientAddress {
  id: string;
  zoneCode: string;
  zoneName: string;
  street: string;
  number: string;
  extra: string | null;
  locality: string;
  neighborhood: string | null;
  references: string | null;
  primaryAddress: boolean;
}

export interface Client {
  id: string;
  firstName: string;
  lastName: string;
  phone: string;
  whatsapp: string;
  email: string | null;
  status: 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';
  acquisitionSource: string | null;
  preferencesJson: string;
  preferences: ClientPreferences;
  notes: string | null;
  addresses: ClientAddress[];
}
