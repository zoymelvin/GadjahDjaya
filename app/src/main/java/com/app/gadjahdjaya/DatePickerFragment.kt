import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker.Builder
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragment(private val onDateSelected: (String, String) -> Unit) : DialogFragment() {

    fun showDatePicker(activity: FragmentActivity) {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now()) // Hanya bisa memilih tanggal sebelum hari ini
            .build()

        val datePicker = Builder.dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .setCalendarConstraints(constraints)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = formatDate(selection.first)
            val endDate = formatDate(selection.second)

            onDateSelected(startDate, endDate) // Mengirim tanggal ke callback
        }

        datePicker.show(activity.supportFragmentManager, "DATE_PICKER")
    }

    private fun formatDate(timeInMillis: Long?): String {
        return if (timeInMillis != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(timeInMillis))
        } else {
            "Pilih Tanggal"
        }
    }
}
