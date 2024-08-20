(ns status-im.common.theme.utils
  (:require
    [status-im.common.theme.core :as theme]
    [status-im.constants :as constants]))

(defn theme-type->theme-value
  "Converts theme type identifier to a theme keyword.
  Returns `:light` or `:dark` based on `theme-type`:
  - `theme-type-light` (1): Light theme.
  - `theme-type-dark` (2): Dark theme.
  - `theme-type-system` (0): Uses system preference."
  [theme-type]
  (condp = theme-type
    constants/theme-type-dark   :dark
    constants/theme-type-light  :light
    constants/theme-type-system (if (theme/device-theme-dark?) :dark :light)
    :dark))
