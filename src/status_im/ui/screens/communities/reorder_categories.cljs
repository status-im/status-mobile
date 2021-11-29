(ns status-im.ui.screens.communities.reorder-categories
  (:require [quo.core :as quo]
            [quo.react-native :as rn]
            [clojure.set :as clojure.set]
            [status-im.i18n.i18n :as i18n]
            [status-im.constants :as constants]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.communities.core :as communities]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.communities.styles :as styles]
            [status-im.ui.screens.communities.community :as community]))

(defn category-change-positon [community-id key up? position]
  (let [new-position (if up? (dec position) (inc position))]
    (>evt [::communities/reorder-community-category community-id key new-position])))

(defn category-item-button [community-id up? disabled? key position]
  (let [[margin-right icon] (if up?
                              [10 :main-icons/dropdown-up]
                              [25 :main-icons/dropdown])]
    [react/touchable-opacity {:disabled disabled?
                              :on-press #(category-change-positon
                                          community-id key up? position)}
     [rn/view {:style (styles/reorder-categories-button margin-right)}
      [icons/icon icon {:color colors/black}]]]))

(defn category-item [community-id count {:keys [key name position]}]
  (let [up-disabled?   (= position 0)
        down-disabled? (= position (dec count))]
    [:<>
     [rn/view {:style styles/reorder-categories-item}
      [icons/icon :main-icons/channel-category {:color colors/gray}]
      [rn/text {:style (styles/reorder-categories-text)} name]
      [category-item-button community-id true up-disabled? key position]
      [category-item-button community-id false down-disabled? key position]]
     [quo/separator]]))

(defn categories-view [community-id categories count]
  [rn/view {:accessibility-label :reorder-categories-list}
   (for [category-vector categories]
     (let [category (clojure.set/rename-keys (get category-vector 1) {:id :key})]
       [category-item community-id count category]))])

(defn view []
  (let [{:keys [community-id]} (<sub [:get-screen-params])
        {:keys [id name images members permissions color]}
        (<sub [:communities/community community-id])
        categories (<sub [:communities/sorted-categories community-id])]
    [:<>
     [topbar/topbar
      {:modal?  true
       :content [community/toolbar-content id name color images
                 (not= (:access permissions) constants/community-no-membership-access)
                 (count members)]}]
     (if (empty? categories)
       [community/blank-page (i18n/label :t/welcome-community-blank-message-categories)]
       [categories-view community-id categories (count categories)])]))
