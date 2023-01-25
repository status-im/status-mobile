(ns status-im2.subs.home
  (:require [re-frame.core :as re-frame]))

(def memo-home-items (atom nil))

(re-frame/reg-sub
 :home-items
 :<- [:search/home-filter]
 :<- [:search/filtered-chats]
 :<- [:communities/communities]
 :<- [:view-id]
 :<- [:home-items-show-number]
 (fn [[search-filter filtered-chats communities view-id home-items-show-number]]
   (if (= view-id :shell-stack)
     (let [communities-count          (count communities)
           chats-count                (count filtered-chats)
           ;; If we have both communities & chats we want to display
           ;; a separator between them

           communities-with-separator (if (and (pos? communities-count)
                                               (pos? chats-count))
                                        (update communities
                                                (dec communities-count)
                                                assoc
                                                :last?
                                                true)
                                        communities)
           res                        {:search-filter search-filter
                                       :items         (concat communities-with-separator
                                                              (take home-items-show-number
                                                                    filtered-chats))}]
       (reset! memo-home-items res)
       res)
     ;;we want to keep data unchanged so react doesn't change component when we leave screen
     @memo-home-items)))

(re-frame/reg-sub
 :hide-home-tooltip?
 :<- [:multiaccount]
 (fn [multiaccount]
   (:hide-home-tooltip? multiaccount)))
