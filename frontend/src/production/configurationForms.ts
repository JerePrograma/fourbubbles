import type {
  FragrancePolicy,
  MachineStatus,
  MachineType,
  ProductionMachine,
  ProductionProgram,
  ProductionStage,
} from '../models/production';

export interface MachineConfigurationForm {
  id: string;
  code: string;
  name: string;
  machineType: MachineType;
  capacityGrams: number;
  status: MachineStatus;
  active: boolean;
  notes: string;
}

export interface ProgramConfigurationForm {
  id: string;
  code: string;
  name: string;
  stage: ProductionStage;
  durationMinutes: number;
  maxTemperatureC: number | null;
  gentle: boolean;
  usesSoftener: boolean;
  fragrancePolicy: FragrancePolicy | null;
  active: boolean;
  notes: string;
}

export function machineFormFrom(machine: ProductionMachine): MachineConfigurationForm {
  return {
    id: machine.id,
    code: machine.code,
    name: machine.name,
    machineType: machine.machineType,
    capacityGrams: machine.capacityGrams,
    status: machine.status,
    active: machine.active,
    notes: machine.notes ?? '',
  };
}

export function machineRequest(form: MachineConfigurationForm) {
  return {
    code: form.code,
    name: form.name.trim(),
    machineType: form.machineType,
    capacityGrams: Number(form.capacityGrams),
    status: form.status,
    active: form.active,
    notes: form.notes.trim() || null,
  };
}

export function programFormFrom(program: ProductionProgram): ProgramConfigurationForm {
  return {
    id: program.id,
    code: program.code,
    name: program.name,
    stage: program.stage,
    durationMinutes: program.durationMinutes,
    maxTemperatureC: program.maxTemperatureC,
    gentle: program.gentle,
    usesSoftener: program.usesSoftener,
    fragrancePolicy: program.fragrancePolicy,
    active: program.active,
    notes: program.notes ?? '',
  };
}

export function programRequest(form: ProgramConfigurationForm) {
  const wash = form.stage === 'WASH';
  return {
    code: form.code,
    name: form.name.trim(),
    stage: form.stage,
    durationMinutes: Number(form.durationMinutes),
    maxTemperatureC: wash ? Number(form.maxTemperatureC) : null,
    gentle: form.gentle,
    usesSoftener: wash ? form.usesSoftener : false,
    fragrancePolicy: wash ? form.fragrancePolicy : null,
    active: form.active,
    notes: form.notes.trim() || null,
  };
}
