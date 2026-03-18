package com.example.zapzap.ui.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utilitários de formatação de data/hora para o chat.
 */
object DateFormatUtils {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR")).apply { timeZone = TimeZone.getTimeZone("America/Sao_Paulo") }
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply { timeZone = TimeZone.getTimeZone("America/Sao_Paulo") }
    private val fullFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).apply { timeZone = TimeZone.getTimeZone("America/Sao_Paulo") }

    /**
     * Formata timestamp para hora (HH:mm).
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Formata timestamp para data relativa (Hoje, Ontem, dd/MM/yyyy).
     */
    fun formatDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, then) -> "Hoje"
            isYesterday(now, then) -> "Ontem"
            else -> dateFormat.format(Date(timestamp))
        }
    }

    /**
     * Formata para exibição na lista de conversas.
     * Hoje -> HH:mm, Ontem -> "Ontem", Outros -> dd/MM/yyyy
     */
    fun formatConversationTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, then) -> formatTime(timestamp)
            isYesterday(now, then) -> "Ontem"
            else -> dateFormat.format(Date(timestamp))
        }
    }

    /**
     * Formata "visto por último" para status do usuário.
     */
    fun formatLastSeen(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "visto agora"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "visto há $minutes min"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> "visto hoje às ${formatTime(timestamp)}"
            diff < TimeUnit.DAYS.toMillis(2) -> "visto ontem às ${formatTime(timestamp)}"
            else -> "visto em ${fullFormat.format(Date(timestamp))}"
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, then: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, then)
    }
}
