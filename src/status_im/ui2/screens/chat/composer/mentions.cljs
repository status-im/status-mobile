(ns status-im.ui2.screens.chat.composer.mentions
  (:require [quo.components.list.item :as list-item]
            [quo.components.text :as text]
            [quo.react]
            [quo.react-native :as rn]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.screens.chat.photos :as photos]
            [utils.re-frame :as rf]))

(defn mention-item
  [[public-key {:keys [alias name nickname] :as user}] _ _ text-input-ref]
  (let [ens-name? (not= alias name)]
    [rn/touchable-opacity
     {:on-press            #(rf/dispatch [:chat.ui/select-mention text-input-ref user])
      :accessibility-label :mention-item}
     ;;TODO quo2 item should be used
     [list-item/list-item
      (cond->
        {:icon              [photos/member-photo public-key]
         :size              :small
         :text-size         :small
         :title             [text/text
                             {:weight          :medium
                              :ellipsize-mode  :tail
                              :number-of-lines 1
                              :size            :small}
                             (or nickname name)
                             (when nickname
                               [text/text
                                {:weight         :regular
                                 :color          :secondary
                                 :ellipsize-mode :tail
                                 :size           :small}
                                " "
                                (when ens-name?
                                  "@")
                                name])]
         :title-text-weight :medium}
        ens-name?
        (assoc :subtitle alias))]]))

(defn autocomplete-mentions
  [suggestions text-input-ref]
  [list/flat-list
   {:keyboardShouldPersistTaps :always
    :data                      suggestions
    :key-fn                    first
    :render-fn                 mention-item
    :render-data               text-input-ref
    :content-container-style   {:padding-bottom 12}
    :accessibility-label       :mentions-list}])
