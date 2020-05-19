(ns status-im.ui.screens.multiaccounts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.multiaccounts.styles :as styles]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.toolbar :as toolbar]
            [quo.core :as quo]
            [status-im.ui.screens.multiaccounts.sheets :as sheets]
            [status-im.react-native.resources :as resources]))

(defn multiaccount-view
  [{:keys [key-uid photo-path name keycard-pairing]}]
  [quo/list-item {:on-press  #(re-frame/dispatch
                               [:multiaccounts.login.ui/multiaccount-selected key-uid])
                  :icon      [photos/photo photo-path {:size styles/multiaccount-image-size}]
                  :title     name
                  :accessory (when keycard-pairing
                               [react/view {:justify-content  :center
                                            :align-items      :center
                                            :margin-right     7
                                            :width            32
                                            :height           32
                                            :border-radius    24
                                            :background-color colors/white
                                            :border-width     1
                                            :border-color     colors/black-transparent}
                                [react/image {:source (resources/get-image :keycard-key)
                                              :style  {:width  11
                                                       :height 19}}]])
                  :chevron   true}])

(defview multiaccounts []
  (letsubs [multiaccounts [:multiaccounts/multiaccounts]]
    [react/view styles/multiaccounts-view
     [topbar/topbar {:show-border? true
                     :navigation   :none
                     :title        (i18n/label :t/your-keys)
                     :accessories  [{:icon                :more
                                     :accessibility-label :your-keys-more-icon
                                     :handler             #(re-frame/dispatch [:bottom-sheet/show-sheet {:content sheets/actions-sheet}])}]}]
     [react/view styles/multiaccounts-container
      [list/flat-list {:data                  (vals multiaccounts)
                       :contentContainerStyle styles/multiaccounts-list-container
                       :key-fn                :address
                       :render-fn             multiaccount-view}]]
     [toolbar/toolbar
      {:show-border? true
       :size         :large
       :center       [quo/button
                      {:on-press #(re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
                       :type     :secondary}
                      (i18n/label :t/access-existing-keys)]}]]))
