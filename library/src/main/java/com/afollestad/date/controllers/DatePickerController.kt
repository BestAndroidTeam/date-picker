/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.date.controllers

import androidx.annotation.CheckResult
import androidx.annotation.IntRange
import androidx.annotation.VisibleForTesting
import com.afollestad.date.DAY_MAX
import com.afollestad.date.DAY_MIN
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.MONTH_MAX
import com.afollestad.date.MONTH_MIN
import com.afollestad.date.OnDateChanged
import com.afollestad.date.data.MonthGraph
import com.afollestad.date.data.MonthItem
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.data.snapshot.MonthSnapshot
import com.afollestad.date.data.snapshot.asCalendar
import com.afollestad.date.data.snapshot.snapshot
import com.afollestad.date.data.snapshot.snapshotMonth
import com.afollestad.date.dayOfMonth
import com.afollestad.date.decrementMonth
import com.afollestad.date.incrementMonth
import com.afollestad.date.month
import com.afollestad.date.runners.Mode
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.util.toCalendar
import com.afollestad.date.year
import java.text.ParseException
import java.util.Calendar
import kotlin.Long.Companion.MAX_VALUE

/** @author Aidan Follestad (@afollestad) */
internal class DatePickerController(
  private val config: DatePickerConfig,
  private val renderHeaders: (MonthSnapshot, DateSnapshot, Boolean) -> Unit,
  private val renderMonthItems: (List<MonthItem>) -> Unit,
  private val getNow: () -> Calendar = { Calendar.getInstance() }
) {
  @VisibleForTesting var didInit: Boolean = false
  private val dateChangedListeners = mutableListOf<OnDateChanged>()
  private var currentMode: Mode by config.currentMode

  @VisibleForTesting var viewingMonth: MonthSnapshot? = null
  @VisibleForTesting var monthGraph: MonthGraph? = null
  @VisibleForTesting var selectedDate: DateSnapshot? = null
    set(value) {
      field = value
      selectedDateCalendar = value?.asCalendar()
    }
  private var selectedDateCalendar: Calendar? = null

  fun maybeInit() {
    if (!didInit) {
      setFullDate(getNow(), notifyListeners = false)
    }
  }

  fun previousMonth() {
    currentMode = CALENDAR
    viewingMonth!!.asCalendar(DAY_MIN.toInt())
        .decrementMonth()
        .let(::updateMonthAndPerformRender)
  }

  fun nextMonth() {
    currentMode = CALENDAR
    viewingMonth!!.asCalendar(DAY_MIN.toInt())
        .incrementMonth()
        .let(::updateMonthAndPerformRender)
  }

  fun setMonth(month: Int) {
    currentMode = CALENDAR
    viewingMonth!!.asCalendar(DAY_MIN.toInt())
        .apply { this.month = month }
        .let(::updateMonthAndPerformRender)
  }

  fun setFullDate(
    calendar: Calendar,
    notifyListeners: Boolean = true,
    fromUserEditInput: Boolean = false
  ) {
    val newSnapshot = calendar.snapshot()
    if (newSnapshot == selectedDate) {
      // no change
      return
    }

    val oldSelected: Calendar = currentSelectedOrNow()
    this.didInit = true
    this.selectedDate = newSnapshot

    val newCalendar = calendar.clone() as Calendar
    if (notifyListeners) {
      notifyListeners(oldSelected, newCalendar)
    }
    updateCurrentMonth(newCalendar)
    render(fromUserEditInput)
  }

  fun setFullDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int? = null,
    @IntRange(from = MONTH_MIN, to = MONTH_MAX) month: Int,
    @IntRange(from = DAY_MIN, to = DAY_MAX) selectedDate: Int? = null,
    notifyListeners: Boolean = true
  ) = setFullDate(getNow().apply {
    if (year != null) {
      this.year = year
    }
    this.month = month
    if (selectedDate != null) {
      this.dayOfMonth = selectedDate
    }
  }, notifyListeners = notifyListeners)

  fun maybeSetDateFromInput(input: CharSequence) {
    if (input.isBlank()) return
    try {
      config.dateFormatter.dateInputFormatter.parse(input.toString())
          ?.let { setFullDate(it.toCalendar(), fromUserEditInput = true) }
    } catch (_: ParseException) {
    }
  }

  @CheckResult fun getFullDate(): Calendar? = selectedDateCalendar

  fun setDayOfMonth(day: Int) {
    if (!didInit) {
      setFullDate(getNow().apply { dayOfMonth = day })
      return
    }

    val oldSelected: Calendar = currentSelectedOrNow()
    val calendar = viewingMonth!!.asCalendar(day)
    selectedDate = calendar.snapshot()
    config.vibrator?.vibrateForSelection()
    notifyListeners(oldSelected, calendar)
    render()
  }

  fun setYear(year: Int) {
    setFullDate(
        month = viewingMonth?.month ?: selectedDate!!.month,
        year = year,
        selectedDate = selectedDate?.day
    )
    currentMode = CALENDAR
  }

  fun addDateChangedListener(listener: OnDateChanged) = dateChangedListeners.add(listener)

  fun clearDateChangedListeners() = dateChangedListeners.clear()

  private fun updateMonthAndPerformRender(month: Calendar) {
    updateCurrentMonth(month)
    render()
    config.vibrator?.vibrateForSelection()
  }

  private fun updateCurrentMonth(calendar: Calendar) {
    viewingMonth = calendar.snapshotMonth()
    monthGraph = MonthGraph(calendar)
  }

  private fun render(fromUserEditInput: Boolean = false) {
    viewingMonth?.let {
      renderHeaders(it, selectedDate!!, fromUserEditInput)
    }
    selectedDate?.let {
      monthGraph!!.getMonthItems(it)
    }
        ?.let {
          renderMonthItems(it)
        }
  }

  private fun notifyListeners(
    old: Calendar,
    new: Calendar
  ) {
    dateChangedListeners.forEach { it(old, new) }
  }

  private fun currentSelectedOrNow(): Calendar = selectedDateCalendar ?: getNow()
}
