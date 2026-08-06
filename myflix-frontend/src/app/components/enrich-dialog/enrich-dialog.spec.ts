import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnrichDialog } from './enrich-dialog';

describe('EnrichDialog', () => {
  let component: EnrichDialog;
  let fixture: ComponentFixture<EnrichDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnrichDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(EnrichDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
