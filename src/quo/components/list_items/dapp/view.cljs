(ns quo.components.list-items.dapp.view
  (:require
    [quo.components.list-items.dapp.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]))

(defn view
  [{:keys [dapp on-press right-component accessibility-label] :as props}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]
    [rn/pressable
     {:style               (style/container (assoc props :pressed? pressed?))
      :accessibility-label accessibility-label
      :on-press            (when on-press
                             (fn [] (on-press dapp)))
      :on-press-in         on-press-in
      :on-press-out        on-press-out}
     [rn/view {:style style/container-info}
      [fast-image/fast-image
       {:source (:avatar dapp)
        :style  {:width 32 :height 32}}]
      [rn/view {:style style/user-info}
       [text/text
        {:weight :semi-bold
         :size   :paragraph-1
         :style  (style/style-text-name theme)}
        (:name dapp)]
       [text/text
        {:weight :regular
         :size   :paragraph-2
         :style  (style/style-text-value theme)}
        (:value dapp)]]]
     (when right-component
       [right-component dapp])]))
