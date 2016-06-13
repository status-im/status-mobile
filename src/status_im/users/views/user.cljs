(ns status-im.users.views.user
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as s]
            [status-im.resources :as res]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.components.styles :refer [icon-ok
                                                 icon-plus]]
            [status-im.users.styles :as st]))

(defn on-press [address]
  (dispatch [:navigate-to :login address])
  (dispatch [:set-in [:login :address] address]))

(defview user-view [{:keys [address photo-path name ] :as account}]
  [current-account [:get :current-account]]
  [touchable-highlight
   {:onPress #(on-press address)}
    [view st/user-container
     [view st/photo-container
      [view st/user-photo-container
       (if (not= address "0x0")
         [image {:source {:uri (if (s/blank? photo-path) :avatar photo-path)}
                 :style  st/photo-image}]
         [image {:source {:uri :icon_plus}
                 :style  icon-plus}])]]
     [view st/name-container
      [text {:style st/name-text
             :numberOfLines 1} name]
      (when (not= address "0x0")
        [text {:style st/address-text
               :numberOfLines 1} address])]
     [view st/online-container
      (when (= address (:address current-account))
        [image {:source {:uri :icon_ok}
                :style  icon-ok}])]]])
