// (C) 2024 Rigger LLC
package com.uchicom.jcam.service;

import com.uchicom.jcam.Constants;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 現在の日付、時刻を管理するサービス.
 *
 * @author shigeki
 */
public class DateTimeService {

  /** コンストラクタ. */
  @Inject
  public DateTimeService() {}

  /**
   * 現在日を取得します.
   *
   * @return 現在日
   */
  public LocalDate getLocalDate() {
    return LocalDate.now(Constants.ZONE_ID);
  }

  /**
   * 現在日時を取得します.
   *
   * @return 現在日時
   */
  public LocalDateTime getLocalDateTime() {
    return LocalDateTime.now(Constants.ZONE_ID);
  }
}
