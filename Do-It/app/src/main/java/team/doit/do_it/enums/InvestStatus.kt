package team.doit.do_it.enums

enum class InvestStatus(private var status: String) {

    PENDING("Pendiente"),
    ACCEPTED("Aceptado"),
    REJECTED("Rechazado");

    fun getDescription(): String {
        return status
    }

    companion object {
        fun from(findValue: String): InvestStatus = values().first { it.status == findValue }
    }

    override fun toString(): String {
        return status
    }
}