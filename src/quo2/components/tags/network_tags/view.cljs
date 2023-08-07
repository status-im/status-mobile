(ns quo2.components.tags.network-tags.view
  (:require [quo2.components.tags.base-tag :as base-tag]
            [quo2.components.list-items.preview-list :as preview-list]
            [quo2.components.tags.network-tags.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))

(defn network-tags
  [_ _]
  (fn [{:keys [title networks status]}]
    [rn/view
     {:style (style/network-tags-container status)}
     [base-tag/base-tag
      {:size 32
       :type :permission}
      [rn/view {:style (style/network-tag-view)}
       [preview-list/preview-list {:type :network :size 32} networks]
       [rn/view
        {:padding-left (case 32
                         8 32
                         6)}
        [text/text
         {:style (style/network-tag-title-style status)}
         title]]]]]))
