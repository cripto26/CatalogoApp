package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import com.quirozsolutions.catalogo1boton.domain.model.CatalogTemplate

object TemplateRegistry {

    fun resolve(template: CatalogTemplate): PdfTemplate {
        return when (template) {
            CatalogTemplate.MINIMALISTA -> MinimalistaTemplate()
            CatalogTemplate.PROMOCIONAL -> PromocionalTemplate()
            CatalogTemplate.COMPACTA -> CompactaTemplate()
            CatalogTemplate.ELEGANTE -> EleganteTemplate()

        }
    }
}
