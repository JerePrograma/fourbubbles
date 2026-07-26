import { describe, expect, it } from 'vitest';
import type { ProductionMachine, ProductionProgram } from '../models/production';
import {
  machineFormFrom,
  machineRequest,
  programFormFrom,
  programRequest,
} from './configurationForms';

const machine: ProductionMachine = {
  id: 'machine-1',
  code: 'WASHER_01',
  name: 'Lavadora principal',
  machineType: 'WASHER',
  capacityGrams: 10000,
  status: 'ACTIVE',
  active: true,
  notes: null,
  version: 2,
};

const dryProgram: ProductionProgram = {
  id: 'program-1',
  code: 'DRY_NORMAL',
  name: 'Secado normal',
  stage: 'DRY',
  requiredMachineType: 'DRYER',
  durationMinutes: 50,
  maxTemperatureC: null,
  gentle: false,
  usesSoftener: false,
  fragrancePolicy: null,
  active: true,
  notes: null,
  version: 1,
};

describe('production configuration forms', () => {
  it('preserves machine identity and normalizes optional notes', () => {
    const form = machineFormFrom(machine);
    expect(form.notes).toBe('');
    expect(machineRequest({ ...form, name: '  Lavadora A  ', notes: '  Operativa  ' }))
      .toEqual({
        code: 'WASHER_01',
        name: 'Lavadora A',
        machineType: 'WASHER',
        capacityGrams: 10000,
        status: 'ACTIVE',
        active: true,
        notes: 'Operativa',
      });
  });

  it('removes wash-only parameters from dry programs', () => {
    const form = programFormFrom(dryProgram);
    expect(programRequest({
      ...form,
      usesSoftener: true,
      fragrancePolicy: 'STANDARD',
      maxTemperatureC: 40,
    })).toMatchObject({
      stage: 'DRY',
      maxTemperatureC: null,
      usesSoftener: false,
      fragrancePolicy: null,
    });
  });

  it('keeps wash technical parameters in update payloads', () => {
    const form = programFormFrom({
      ...dryProgram,
      id: 'program-2',
      code: 'WASH_30_NONE',
      stage: 'WASH',
      requiredMachineType: 'WASHER',
      maxTemperatureC: 30,
      gentle: true,
      fragrancePolicy: 'NONE',
    });
    expect(programRequest(form)).toMatchObject({
      stage: 'WASH',
      maxTemperatureC: 30,
      gentle: true,
      fragrancePolicy: 'NONE',
    });
  });
});
