(ns legacy.status-im.ui.screens.profile.visibility-status.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [quo.foundations.colors :as quo.colors]))

(defn visibility-status-dot
  [{:keys [color size new-ui?]} theme]
  (if new-ui?
    {:background-color color
     :width            size
     :height           size
     :border-radius    (/ size 2)
     :border-width     3.5
     :border-color     (quo.colors/theme-colors quo.colors/white quo.colors/neutral-90 theme)}
    {:background-color color
     :width            size
     :height           size
     :border-radius    (/ size 2)
     :border-width     1
     :border-color     colors/white}))
