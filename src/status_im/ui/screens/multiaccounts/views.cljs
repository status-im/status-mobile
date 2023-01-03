(ns status-im.ui.screens.multiaccounts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.multiaccounts.sheets :as sheets]
            [status-im.ui.screens.multiaccounts.styles :as styles]
            [utils.security.core :as security]))

(defn multiaccount-view
  [{:keys [key-uid name keycard-pairing] :as account}]
  [quo/list-item
   {:on-press        #(re-frame/dispatch
                       [:multiaccounts.login.ui/multiaccount-selected key-uid])
    :icon            [photos/photo (multiaccounts/displayed-photo account)
                      {:size styles/multiaccount-image-size}]
    :title           name
    :accessory-style (when keycard-pairing {:flex-basis 100})
    :accessory       (when keycard-pairing
                       [react/view
                        {:justify-content  :center
                         :align-items      :center
                         :width            32
                         :height           32
                         :border-radius    24
                         :background-color colors/white
                         :border-width     1
                         :border-color     colors/black-transparent}
                        [react/image
                         {:source (resources/get-image :keycard-key)
                          :style  {:width  11
                                   :height 19}}]])
    :chevron         true}])

(defn topbar-button
  []
  (re-frame/dispatch [:bottom-sheet/show-sheet :generate-a-new-key {}]))

(defview multiaccounts
  []
  (letsubs [multiaccounts [:multiaccounts/multiaccounts]]
    [:<>
     [react/view styles/multiaccounts-container
      [list/flat-list
       {:data                  (vals multiaccounts)
        :contentContainerStyle styles/multiaccounts-list-container
        :key-fn                (comp str :address)
        :render-fn             multiaccount-view}]]
     [toolbar/toolbar
      {:show-border? true
       :size         :large
       :center       [quo/button
                      {:on-press #(re-frame/dispatch
                                   [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
                       :type     :secondary}
                      (i18n/label :t/access-existing-keys)]}]]))

(defn seed-phrase-input
  [{:keys [on-change-event
           seed-word-count
           seed-shape-invalid?]}]
  [react/view
   {:flex               1
    :justify-content    :center
    :padding-horizontal 16}
   [quo/text-input
    {:show-cancel         false
     :auto-correct        false
     :keyboard-type       :visible-password
     :placeholder         (i18n/label :t/seed-phrase-placeholder)
     :monospace           true
     :multiline           true
     :auto-focus          true
     :accessibility-label :passphrase-input
     :on-change-text      #(re-frame/dispatch (conj on-change-event (security/mask-data %)))}]
   ;; word counter view
   [react/view {:align-items :flex-end}
    [react/view
     {:flex-direction   :row
      :align-items      :center
      :padding-vertical 8
      :opacity          (if seed-word-count 1 0)}
     [quo/text
      {:color (if seed-shape-invalid? :secondary :main)
       :size  :small}
      (when-not seed-shape-invalid? "✓ ")
      (i18n/label-pluralize seed-word-count :t/words-n)]]]])
