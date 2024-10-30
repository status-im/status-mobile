(ns legacy.status-im.ui.components.checkbox.view
  (:require
    [legacy.status-im.ui.components.checkbox.styles :as styles]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]))

(defn checkbox
  "react/check-box is currently not available on iOS,
  use it once it is available on all platforms"
  [{:keys [on-value-change checked? accessibility-label style]
    :or   {accessibility-label :checkbox}}]
  [(if on-value-change react/touchable-highlight react/view)
   (merge {:style               (merge
                                 styles/wrapper
                                 style)
           :accessibility-label (str accessibility-label
                                     "-"
                                     (if checked? "on" "off"))}
          (when on-value-change
            {:on-press #(on-value-change (not checked?))}))
   (if checked?
     [icons/tiny-icon
      :tiny-icons/tiny-check
      {:container-style (styles/icon-check-container true)
       :color           colors/white-persist}]
     [react/view {:style (styles/icon-check-container false)}])])
