(ns status-im.ui.screens.multiaccounts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.multiaccounts.styles :as styles]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.button :as button]
            [status-im.ui.screens.multiaccounts.sheets :as sheets]
            [status-im.react-native.resources :as resources]))

(defn multiaccount-view
  [{:keys [key-uid photo-path name keycard-pairing]}]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch
                [:multiaccounts.login.ui/multiaccount-selected key-uid])}
   [react/view styles/multiaccount-view
    [photos/photo photo-path {:size styles/multiaccount-image-size}]
    [react/view styles/multiaccount-badge-text-view
     [react/view {:flex-direction :row}
      [react/text {:style          styles/multiaccount-badge-text
                   :ellipsize-mode :middle
                   :numberOfLines  1}
       name]]
     ;;TODO we don't have public key in multiaccounts
     #_[react/text {:style styles/multiaccount-badge-pub-key-text}
        (utils/get-shortened-address public-key)]]
    [react/view {:flex 1}]
    (when keycard-pairing
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
    [icons/icon :main-icons/next {:color colors/gray-transparent-40}]]])

(defview multiaccounts []
  (letsubs [multiaccounts [:multiaccounts/multiaccounts]]
    [react/view styles/multiaccounts-view
     [topbar/topbar {:show-border? true
                     :navigation   :none
                     :title        (i18n/label :t/your-keys)
                     :accessories  [{:icon    :more
                                     :accessibility-label :your-keys-more-icon
                                     :handler #(re-frame/dispatch [:bottom-sheet/show-sheet {:content sheets/actions-sheet}])}]}]
     [react/view styles/multiaccounts-container
      [react/view styles/multiaccounts-list-container
       [list/flat-list {:data      (vals multiaccounts)
                        :key-fn    :address
                        :render-fn (fn [multiaccount] [multiaccount-view multiaccount])}]]
      [react/view {:style (styles/bottom-button-container)}
       [button/button {:on-press #(re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
                       :type     :secondary
                       :label    (i18n/label :t/access-key)}]]]]))
