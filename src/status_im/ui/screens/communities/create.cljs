(ns status-im.ui.screens.communities.create
  (:require [quo.react-native :as rn]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [clojure.string :as str]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.communities.core :as communities]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.communities.membership :as memberships]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn valid? [community-name community-description membership]
  (and (not (str/blank? community-name))
       (not (str/blank? community-description))
       membership))

(def crop-size 1000)
(def crop-opts {:cropping             true
                :cropperCircleOverlay true
                :width                crop-size
                :height               crop-size})

(defn pick-pic []
  (react/show-image-picker
   prn
   crop-opts))

(defn take-pic []
  (react/show-image-picker-camera
   prn
   crop-opts))

(defn bottom-sheet [has-picture]
  (fn []
    [:<>
     [quo/list-item {:accessibility-label :take-photo
                     :theme               :accent
                     :icon                :main-icons/camera
                     :title               (i18n/label :t/community-image-take)
                     :on-press            take-pic}]
     [quo/list-item {:accessibility-label :pick-photo
                     :icon                :main-icons/gallery
                     :theme               :accent
                     :title               (i18n/label :t/community-image-pick)
                     :on-press            pick-pic}]
     (when has-picture
       [quo/list-item {:accessibility-label :remove-photo
                       :icon                :main-icons/delete
                       :theme               :accent
                       :title               (i18n/label :t/community-image-remove)
                       :on-press            prn}])]))

(defn photo-picker []
  (let [{:keys [image]} (<sub [:communities/create])]
    [rn/view {:style {:padding-top 16
                      :align-items :center}}
     [rn/touchable-opacity {:on-press #(>evt [:bottom-sheet/show-sheet
                                              {:content (bottom-sheet (boolean image))}])}
      [rn/view {:style {:width  128
                        :height 128}}
       [rn/view {:style {:flex             1
                         :border-radius    64
                         :background-color (colors/get-color :ui-01)
                         :justify-content  :center
                         :align-items      :center}}
        [icons/icon :main-icons/photo {:color (colors/get-color :icon-02)}]
        [quo/text {:color :secondary}
         (i18n/label :t/community-thumbnail-upload)]]
       [rn/view {:style {:position :absolute
                         :top      0
                         :right    7}}
        [rn/view {:style {:width            40
                          :height           40
                          :background-color (colors/get-color :interactive-01)
                          :border-radius    20
                          :align-items      :center
                          :justify-content  :center
                          :shadow-offset    {:width 0 :height 1}
                          :shadow-radius    6
                          :shadow-opacity   1
                          :shadow-color     (colors/get-color :shadow-01)
                          :elevation        2}}
         [icons/icon :main-icons/add {:color colors/white}]]]]]]))

(defn view []
  (let [{:keys [name description membership]} (<sub [:communities/create])]
    [rn/view {:style {:flex 1}}
     [topbar/topbar {:title (i18n/label :t/new-community-title)}]
     [rn/scroll-view {:style                   {:flex 1}
                      :content-container-style {:padding-vertical 16}}
      [rn/view {:style {:padding-bottom     16
                        :padding-top        10
                        :padding-horizontal 16}}
       [quo/text-input
        {:label          (i18n/label :t/name-your-community)
         :placeholder    (i18n/label :t/name-your-community-placeholder)
         :default-value  name
         :on-change-text #(>evt  [::communities/create-field :name %])
         :auto-focus     true}]]
      [rn/view {:style {:padding-bottom     16
                        :padding-top        10
                        :padding-horizontal 16}}
       [quo/text-input
        {:label          (i18n/label :t/give-a-short-description-community)
         :placeholder    (i18n/label :t/give-a-short-description-community)
         :multiline      true
         :default-value  description
         :on-change-text #(>evt [::communities/create-field :description %])}]]
      [quo/list-header {:color :main}
       (i18n/label :t/community-thumbnail-image)]
      [photo-picker]
      [rn/view {:style {:padding-bottom     16
                        :padding-top        10
                        :padding-horizontal 16}}
       [quo/text-input
        {:label       (i18n/label :t/community-color)
         :placeholder (i18n/label :t/community-color-placeholder)
         :show-cancel false
         :auto-focus  true}]]
      [rn/view
       [quo/separator {:style {:margin-vertical 10}}]
       [quo/list-item {:title          (i18n/label :t/membership-button)
                       :accessory-text (when-not membership (i18n/label :t/membership-none))
                       :on-press       #(>evt [:navigate-to :community-membership])
                       :chevron        true
                       :size           :small}]
       [quo/list-footer
        (i18n/label (get-in memberships/options [membership :description] :t/membership-none-placeholder))]]]
     [toolbar/toolbar
      {:show-border? true
       :center
       [quo/button {:disabled (not (valid? name description membership))
                    :type     :secondary
                    :on-press #(>evt [::communities/create-confirmation-pressed])}
        (i18n/label :t/create)]}]]))
