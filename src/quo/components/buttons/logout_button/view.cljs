(ns quo.components.buttons.logout-button.view
  (:require
    [quo.components.buttons.logout-button.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]))

(defn- view-internal
  [_]
  (let [pressed?     (reagent/atom false)
        on-press-in  #(reset! pressed? true)
        on-press-out #(reset! pressed? nil)]
    (fn
      [{:keys [on-press on-long-press disabled? theme container-style]}]
      [rn/pressable
       {:accessibility-label :log-out-button
        :on-press            on-press
        :on-press-in         on-press-in
        :on-press-out        on-press-out
        :on-long-press       on-long-press
        :disabled            disabled?
        :style               (merge (style/main {:pressed?  @pressed?
                                                 :theme     theme
                                                 :disabled? disabled?})
                                    container-style)}
       [icon/icon :i/log-out {:color (if pressed? colors/white-opa-40 colors/white-opa-70)}]
       [text/text {:weight :medium :size :paragraph-1}
        (i18n/label :t/logout)]])))

(def view (quo.theme/with-theme view-internal))
