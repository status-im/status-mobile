(ns status-im.ui2.screens.common.contact-list.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [quo2.core :as quo2]
            [utils.re-frame :as rf]))

(defn contacts-section-header [{:keys [title]}]
  [rn/view {:style {:border-top-width   1
                    :border-top-color   colors/neutral-20
                    :padding-vertical   8
                    :padding-horizontal 20 :margin-top 8}}
   [quo2/text {:size   :paragraph-2
               :weight :medium
               :style  {:color colors/neutral-50}} title]])

(defn contact-list [data]
  (let [contacts (rf/sub [:contacts/filtered-active-sections])]
    [rn/section-list
     {:key-fn                         :title
      :sticky-section-headers-enabled false
      :sections                       contacts
      :render-section-header-fn       contacts-section-header
      :content-container-style        {:padding-bottom 120}
      :render-fn                      (fn [item]
                                        [contact-list-item/contact-list-item item data])}]))
