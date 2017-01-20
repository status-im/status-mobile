(ns status-im.accounts.views.account
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as s]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.components.styles :refer [icon-ok
                                                 icon-plus]]
            [status-im.accounts.styles :as st]))

(defn on-press [address]
  (dispatch [:navigate-to :login address])
  (dispatch [:set-in [:login :address] address]))

(defview account-view [{:keys [address photo-path name] :as account}]
  [current-account [:get-current-account]]
  [touchable-highlight
   {:onPress #(on-press address)}
    [view st/account-container
     [view st/photo-container
      [view st/account-photo-container
       (if (not= address "0x0")
         [image {:source {:uri (if (s/blank? photo-path) :avatar photo-path)}
                 :style  st/photo-image}]
         [image {:source {:uri :icon_plus}
                 :style  icon-plus}])]]
     [view st/name-container
      [text {:style st/name-text
             :numberOfLines 1} (or name address)]
      (when (not= address "0x0")
        [text {:style st/address-text
               :numberOfLines 1} address])]
     [view st/online-container
      (when (= address (:address current-account))
        [image {:source {:uri :icon_ok}
                :style  icon-ok}])]]])
