(ns quo2.components.avatars.group-avatar.view
  (:require [quo2.components.icon :as icon]
            [quo2.theme :as quo.theme]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.components.avatars.group-avatar.style :as style]))

(def sizes
  {:icon      {:small  12
               :medium 16
               :large  20}
   :container {:small  20
               :medium 32
               :large  48}})

;; TODO: this implementation does not support group display picture (can only display default group
;; icon).
(defn- view-internal
  [_]
  (fn [{:keys [color size theme]}]
    (let [container-size (get-in sizes [:container size])
          icon-size      (get-in sizes [:icon size])]
      [rn/view
       {:style (style/container {:container-size      container-size
                                 :customization-color color
                                 :theme               theme})}
       [icon/icon :i/group
        {:size  icon-size
         :color colors/white-opa-70}]])))

(def view (quo.theme/with-theme view-internal))
