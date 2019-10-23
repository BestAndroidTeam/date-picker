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
package com.afollestad.date.renderers

import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.View
import android.widget.TextView
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.R
import com.afollestad.date.data.DayOfWeek
import com.afollestad.date.data.MonthItem
import com.afollestad.date.data.MonthItem.Week
import com.afollestad.date.data.MonthItem.DayOfMonth
import com.afollestad.date.data.MonthItem.WeekHeader
import com.afollestad.date.data.NO_DATE
import com.afollestad.date.dayOfWeek
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.Util.createTextSelector
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.util.resolveColor
import java.util.Calendar



/** @author Aidan Follestad (@afollestad) */
internal class MonthItemRenderer(private val config: DatePickerConfig) {
  private val calendar = Calendar.getInstance()

  fun render(
    item: MonthItem,
    rootView: View,
    textView: TextView,
    onSelection: (DayOfMonth) -> Unit
  ) {
    when (item) {
      is WeekHeader -> renderWeekHeader(item.dayOfWeek, textView)
      is DayOfMonth -> renderDayOfMonth(item, rootView, textView, onSelection)
      is Week -> renderDayOfWeek(item,rootView, textView)
    }
  }

  private fun renderWeekHeader(
    dayOfWeek: DayOfWeek,
    textView: TextView
  ) {
    textView.apply {
      when(dayOfWeek){
        DayOfWeek.WEEK_NUMBER -> {
          text = ""
          setTextColor(themeColorPrimary())
        }
        else -> {
          calendar.dayOfWeek = dayOfWeek
          text = config.dateFormatter.weekdayAbbreviation(calendar)
          setTextColor(context.resolveColor(android.R.attr.textColorSecondary))
        }
      }
      typeface = config.normalFont
    }
  }

  private fun renderDayOfMonth(
    dayOfMonth: DayOfMonth,
    rootView: View,
    textView: TextView,
    onSelection: (DayOfMonth) -> Unit
  ) {
    rootView.background = null
    textView.apply {
      setTextColor(createTextSelector(context, config.selectionColor))
      text = dayOfMonth.date.positiveOrEmptyAsString()
      typeface = config.normalFont
      gravity = CENTER
      background = null
      setOnClickListener(null)
    }

    if (dayOfMonth.date == NO_DATE) {
      rootView.isEnabled = false
      textView.isSelected = false
      textView.isActivated = false
      return
    }

    rootView.isEnabled = textView.text.toString()
        .isNotEmpty()
    textView.apply {
      isSelected = dayOfMonth.isSelected
      isActivated = dayOfMonth.isToday
      background = createCircularSelector(
          context = context,
          selectedColor = config.selectionColor,
          todayStrokeColor = config.todayStrokeColor
      )
      onClickDebounced { onSelection(dayOfMonth) }
    }
  }

  private fun renderDayOfWeek(
    week: Week,
    rootView: View,
    textView: TextView
  ) {
    rootView.background = null
    textView.apply {
      setTextColor(rootView.themeColorPrimary())
      text = week.weekNumber.toString()
      typeface = config.normalFont
    }

  }

  private fun Int.positiveOrEmptyAsString(): String {
    return if (this < 1) "" else toString()
  }

  private fun View.themeColorPrimary(): Int {
    val typedValue = TypedValue()
    val attributes = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
    val color = attributes.getColor(0, 0)
    attributes.recycle()
    return color
  }
}
