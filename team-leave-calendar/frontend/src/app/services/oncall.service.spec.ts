import { TestBed } from '@angular/core/testing';

import { OncallService } from './oncall.service';

describe('OncallService', () => {
  let service: OncallService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OncallService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
