package me.friwi.arterion.plugin.formula;

import me.friwi.arterion.plugin.util.config.api.ConfigHashmap;
import me.friwi.arterion.plugin.util.config.api.Configureable;

import java.util.Map;

@Configureable(location = "formulas.conf")
public class ArterionFormulaConfig {
    @ConfigHashmap
    public Map<String, String> formulas;
}
