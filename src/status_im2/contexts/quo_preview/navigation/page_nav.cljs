(ns status-im2.contexts.quo-preview.navigation.page-nav
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def ^:private descriptor
  [{:key     :type
    :type    :select
    :options [{:key :no-title}
              {:key :title}
              {:key :dropdown}
              {:key :token}
              {:key :channel}
              {:key   :title-description
               :value "Title + Description"}
              {:key :wallet-networks}
              {:key :community}
              {:key :network}]}
   {:key     :background
    :type    :select
    :options (map (fn [bg-type]
                    {:key   bg-type
                     :value (string/capitalize (name bg-type))})
                  [:white :neutral-5 :neutral-90 :neutral-95 :neutral-100 :photo :blur])}
   {:key     :icon-name
    :type    :select
    :options [{:key   :i/placeholder
               :value "Placeholder"}
              {:key   :i/arrow-left
               :value "Arrow left"}]}])


(def right-side-options
  (let [options [{:icon-name :i/save :on-press #(js/alert "SAVE")}
                 {:icon-name :i/mark-as-read :on-press #(js/alert "MARK AS READ")}
                 {:icon-name :i/mention :on-press #(js/alert "A MENTION!")}]]
    [{:key   []
      :value "No actions"}
     {:key   (take 1 options)
      :value "1 action"}
     {:key   (take 2 options)
      :value "2 actions"}
     {:key   (take 3 options)
      :value "3 actions"}]))

(def account-switcher
  {:key :account-switcher})

(def no-title-descriptor
  [{:key     :right-side
    :type    :select
    :options (conj right-side-options account-switcher)}])

(def title-descriptor
  [{:key     :right-side
    :type    :select
    :options (conj right-side-options account-switcher)}
   {:key :title :type :text}
   {:key     :text-align
    :type    :select
    :options [{:key :left}
              {:key :center}]}])

(def dropdown-descriptor
  [{:key :dropdown-selected? :type :boolean}
   {:key :dropdown-text :type :text}])

(def token-descriptor
  [{:key     :right-side
    :type    :select
    :options (conj right-side-options account-switcher)}
   {:key     :token-logo
    :type    :select
    :options [{:key   (resources/get-mock-image :status-logo)
               :value "Status logo"}
              {:key   (resources/get-mock-image :rarible)
               :value "Rarible"}]}
   {:key  :token-name
    :type :text}
   {:key  :token-abbreviation
    :type :text}])

(def channel-descriptor
  [{:key     :right-side
    :type    :select
    :options right-side-options}
   {:key     :channel-emoji
    :type    :select
    :options [{:key   "🍇"
               :value "🍇"}
              {:key   "🍑"
               :value "🍑"}]}

   {:key :channel-name :type :text}
   {:key     :channel-icon
    :type    :select
    :options [{:key   :i/locked
               :value "Locked"}
              {:key   :i/unlocked
               :value "Unlocked"}]}])

(def title-description-descriptor
  [{:key     :right-side
    :type    :select
    :options (butlast right-side-options)}
   {:key :title :type :text}
   {:key :description :type :text}
   {:key     :picture
    :type    :select
    :options [{:key   nil
               :value "No picture"}
              {:key   (resources/get-mock-image :photo1)
               :value "Photo 1"}
              {:key   (resources/get-mock-image :photo2)
               :value "Photo 2"}]}])

(def wallet-networks-descriptor
  [{:key     :right-side
    :type    :select
    :options (conj (take 2 right-side-options) account-switcher)}])

(def community-descriptor
  [{:key     :right-side
    :type    :select
    :options right-side-options}
   {:key     :community-logo
    :type    :select
    :options [{:key   (resources/get-mock-image :diamond)
               :value "Diamond"}
              {:key   (resources/get-mock-image :coinbase)
               :value "Coinbase"}]}
   {:key :community-name :type :text}])

(def network-descriptor
  [{:key     :right-side
    :type    :select
    :options right-side-options}
   {:key     :network-logo
    :type    :select
    :options [{:key   (resources/get-mock-image :diamond)
               :value "Diamond"}
              {:key   (resources/get-mock-image :coinbase)
               :value "Coinbase"}]}
   {:key  :network-name
    :type :text}])

(defn- photo-bg
  [background]
  (when (#{:photo} background)
    [rn/image
     {:style  {:position :absolute
               :top      0
               :bottom   0
               :left     20
               :right    0
               :width    "100%"
               :height   200}
      :source (resources/get-mock-image :photo2)}]))

(defn view
  [{:keys [theme]}]
  (let [state (reagent/atom
               {:type               :title-description
                :background         (if (= theme :light) :white :neutral-90)
                :icon-name          :i/placeholder
                :on-press           #(js/alert "Left icon pressed!")
                :right-side         [{:icon-name :i/save :on-press #(js/alert "SAVE")}
                                     {:icon-name :i/mark-as-read :on-press #(js/alert "MARK AS READ")}
                                     {:icon-name :i/mention :on-press #(js/alert "A MENTION!")}]
                :title              "Page title"
                :text-align         :center
                :dropdown-on-change #(js/alert "Dropdown pressed!")
                :dropdown-selected? false
                :dropdown-text      "Recent"
                :token-logo         (resources/get-mock-image :status-logo)
                :token-name         "Status"
                :token-abbreviation "SNT"
                :channel-emoji      "🍇"
                :channel-name       "general"
                :channel-icon       :i/locked
                :description        "Description"
                :picture            (resources/get-mock-image :photo1)
                :community-name     "Rarible"
                :community-logo     (resources/get-mock-image :coinbase)
                :network-name       "Mainnet"
                :network-logo       (resources/get-mock-image :diamond)})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                (concat descriptor
                                           (case (:type @state)
                                             :no-title          no-title-descriptor
                                             :title             title-descriptor
                                             :dropdown          dropdown-descriptor
                                             :token             token-descriptor
                                             :channel           channel-descriptor
                                             :title-description title-description-descriptor
                                             :wallet-networks   wallet-networks-descriptor
                                             :community         community-descriptor
                                             :network           network-descriptor
                                             nil))
        :blur?                     (= :blur (:background @state))
        :show-blur-background?     (= :blur (:background @state))
        :component-container-style {:background-color (case (:background @state)
                                                        :white       colors/white
                                                        :neutral-5   colors/neutral-5
                                                        :neutral-90  colors/neutral-90
                                                        :neutral-95  colors/neutral-95
                                                        :neutral-100 colors/neutral-100
                                                        nil)
                                    :margin-vertical  40
                                    :width            "100%"}}

       [photo-bg (:background @state)]
       [quo/page-nav @state]])))
