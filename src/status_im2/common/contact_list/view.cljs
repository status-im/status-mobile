(ns status-im2.common.contact-list.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [utils.re-frame :as rf]))

(defn contacts-section-header
  [{:keys [title]}]
  [quo/divider-label {:label title}])

(defn contact-list
  [data]
  (let [contacts (if (:group data)
                   (rf/sub [:contacts/grouped-by-first-letter])
                   (rf/sub [:contacts/filtered-active-sections]))]
    [rn/section-list
     {:key-fn                         :title
      :sticky-section-headers-enabled false
      :sections                       contacts
      :render-section-header-fn       contacts-section-header
      :content-container-style        {:padding-bottom 20}
      :render-data                    data
      :render-fn                      contact-list-item/contact-list-item}]))
