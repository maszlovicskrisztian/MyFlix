import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadingOverlay } from './loading-overlay';

describe('LoadingOverlay', () => {
  let component: LoadingOverlay;
  let fixture: ComponentFixture<LoadingOverlay>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingOverlay],
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingOverlay);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the caption only when a label is given', async () => {
    expect(fixture.nativeElement.querySelector('.overlay__text')).toBeNull();

    fixture.componentRef.setInput('label', 'Betöltés…');
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.overlay__text').textContent).toContain('Betöltés…');
  });
});
