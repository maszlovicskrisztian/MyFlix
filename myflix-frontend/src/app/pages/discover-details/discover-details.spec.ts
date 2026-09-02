import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscoverDetails } from './discover-details';

describe('DiscoverDetails', () => {
  let component: DiscoverDetails;
  let fixture: ComponentFixture<DiscoverDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DiscoverDetails],
    }).compileComponents();

    fixture = TestBed.createComponent(DiscoverDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
