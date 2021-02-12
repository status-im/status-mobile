(ns status-im.ui.screens.appearance.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.react-native.resources :as resources]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [status-im.constants :as constants]))

(def titles {constants/profile-pictures-visibility-contacts-only (i18n/label :t/recent-recipients)
             constants/profile-pictures-visibility-everyone      (i18n/label :t/everyone)
             constants/profile-pictures-visibility-none          (i18n/label :t/none)})

(defn button [label icon theme selected?]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:multiaccounts.ui/appearance-switched theme])}
   [react/view (merge {:align-items :center :padding 8 :border-radius 20}
                      (when selected?
                        {:background-color colors/blue-light}))
    [react/image {:source (get resources/ui icon)}]
    [react/text {:style {:margin-top 8}}
     (i18n/label label)]]])

(views/defview appearance []
  (views/letsubs [{:keys [appearance profile-pictures-visibility]} [:multiaccount]]
    [react/view {:flex 1}
     [topbar/topbar {:title (i18n/label :t/appearance)}]
     [quo/list-header (i18n/label :t/preference)]
     [react/view {:flex-direction  :row :padding-horizontal 8
                  :justify-content :space-between :margin-vertical 16}
      [button :t/light :theme-light 1 (= 1 appearance)]
      [button :t/dark :theme-dark 2 (= 2 appearance)]
      [button :t/system :theme-system 0 (= 0 appearance)]]
     [quo/list-header (i18n/label :t/chat)]
     [quo/list-item
      {:title               (i18n/label :t/show-profile-pictures)
       :accessibility-label :show-profile-pictures
       :accessory           :text
       :accessory-text      (get titles profile-pictures-visibility)
       :on-press            #(re-frame/dispatch [:navigate-to :appearance-profile-pic])
       :chevron             true}]]))

(defn radio-item [id value]
  [quo/list-item
   {:active    (= value id)
    :accessory :radio
    :title     (get titles id)
    :on-press  #(re-frame/dispatch [:multiaccounts.ui/appearance-profile-switched id])}])

(views/defview profile-pic []
  (views/letsubs [{:keys [profile-pictures-visibility]} [:multiaccount]]
    [react/view {:flex 1}
     [topbar/topbar {:title (i18n/label :t/show-profile-pictures)}]
     [react/view {:margin-top 8}
      [radio-item constants/profile-pictures-visibility-everyone profile-pictures-visibility]
      [radio-item constants/profile-pictures-visibility-contacts-only profile-pictures-visibility]
      [radio-item constants/profile-pictures-visibility-none profile-pictures-visibility]]]))