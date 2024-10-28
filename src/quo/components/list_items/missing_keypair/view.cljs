(ns quo.components.list-items.missing-keypair.view
  (:require
    [quo.components.avatars.icon-avatar :as icon-avatar]
    [quo.components.icon :as icon]
    [quo.components.list-items.missing-keypair.schema :as component-schema]
    [quo.components.list-items.missing-keypair.style :as style]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- internal-view
  [{{:keys [accounts name type]} :keypair
    :keys                        [keypair blur? on-options-press]}]
  (let [theme                    (quo.theme/use-theme)
        on-keypair-options-press (rn/use-callback
                                  (fn [event]
                                    (on-options-press event keypair))
                                  [keypair on-options-press])]
    [rn/view
     {:style               (style/container {:theme theme
                                             :blur? blur?})
      :accessibility-label :missing-keypair-item}
     [icon-avatar/icon-avatar
      {:size    :size-32
       :icon    (case type
                  :seed :i/seed
                  :key  :i/password
                  nil)
       :blur?   true
       :border? true}]
     [rn/view
      {:style               style/name-container
       :accessibility-label :name}
      [text/text
       {:weight :semi-bold}
       name]]
     [rn/view
      {:accessibility-label :preview-list}
      [preview-list/view
       {:blur?           blur?
        :type            :accounts
        :size            :size-24
        :number          (count accounts)
        :container-style style/preview-list-container}
       accounts]]
     [rn/pressable {:on-press on-keypair-options-press}
      [icon/icon :i/options
       {:color               (style/options-icon-color
                              {:theme theme
                               :blur? blur?})
        :accessibility-label :options-button}]]]))

(def view (schema/instrument #'internal-view component-schema/?schema))
