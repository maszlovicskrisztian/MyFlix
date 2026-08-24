import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnknownMedia } from './unknown-media';

describe('UnknownMedia', () => {
  let component: UnknownMedia;
  let fixture: ComponentFixture<UnknownMedia>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnknownMedia],
    }).compileComponents();

    fixture = TestBed.createComponent(UnknownMedia);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
