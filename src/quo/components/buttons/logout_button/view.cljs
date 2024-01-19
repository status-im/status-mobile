(ns quo.components.buttons.logout-button.view
  (:require
    [quo.components.buttons.logout-button.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    quo.theme
    [react-native.pure :as rn.pure]
    [utils.i18n :as i18n]))

(defn- view-pure
  [{:keys [on-press on-long-press disabled? container-style]}]
  (let [[pressed? set-pressed] (rn.pure/use-state false)
        theme                  (quo.theme/use-theme)]
    (rn.pure/pressable
     {:accessibility-label :log-out-button
      :on-press            on-press
      :on-press-in         #(set-pressed true)
      :on-press-out        #(set-pressed nil)
      :on-long-press       on-long-press
      :disabled            disabled?
      :style               (merge (style/main {:pressed?  pressed?
                                               :theme     theme
                                               :disabled? disabled?})
                                  container-style)}
     (icon/icon :i/log-out {:color (if pressed? colors/white-opa-40 colors/white-opa-70)})
     (text/text
      {:weight :medium :size :paragraph-1}
      (i18n/label :t/logout)))))

(defn view
  [props]
  (rn.pure/func view-pure props))
