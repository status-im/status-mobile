(ns status-im.contexts.preview-screens.quo-preview.switcher.group-messaging-card
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :title :type :text}
   {:key     :status
    :type    :select
    :options [{:key :read}
              {:key :unread}
              {:key :mention}]}
   {:key :counter-label :type :text}
   {:key     :type
    :type    :select
    :options [{:key   :message
               :value :text}
              {:key :photo}
              {:key :sticker}
              {:key :gif}
              {:key :audio}
              {:key :community}
              {:key :link}
              {:key   :code
               :value :code-snippet}]}
   {:key :last-message :type :text}
   {:key :avatar? :type :boolean}
   (preview/customization-color-option)])

;; Mock data
(def sticker {:source (resources/get-mock-image :sticker)})
(def community-avatar (resources/get-mock-image :community-logo))
(def gif {:source (resources/get-mock-image :gif)})
(def coinbase-community (resources/get-mock-image :coinbase))
(def photos-list
  [{:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}
   {:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}])
(def clojure-example
  "(defn request->xhrio-options
  [{:as   request
    :keys [on-success on-failure]
    :or   {on-success      [:http-no-on-success]
           on-failure      [:http-no-on-failure]}}]
  ; wrap events in cljs-ajax callback
  (let [api (new goog.net.XhrIo)]
    (-> request
        (assoc
          :api     api
          :handler (partial ajax-xhrio-handler
                            #(dispatch (conj on-success %))
                            #(dispatch (conj on-failure %))
                            api))
        (dissoc :on-success :on-failure :on-request))))")

(defn get-mock-content
  [data]
  (case (:type data)
    :message
    {:text (:last-message data)}

    :photo
    {:photos photos-list}

    :sticker
    sticker

    :gif
    gif

    :audio
    {:duration "00:32"}

    :community
    {:community-avatar coinbase-community
     :community-name   "Coinbase"}

    :link
    {:icon :placeholder
     :text "Rolling St..."}

    :code
    {:language :clojure
     :text     clojure-example}

    nil))

(defn get-mock-data
  [data]
  (merge
   data
   {:content (merge (get-mock-content data)
                    {:mention-count (when (= (:status data) :mention) (:counter-label data))})}))

(defn view
  []
  (let [state (reagent/atom {:title               "Hester, John, Steven, and 2 others"
                             :type                :message
                             :status              :read
                             :last-message        "Hello there, there is a new message"
                             :customization-color :camel
                             :avatar?             false
                             :counter-label       5})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/group-messaging-card
        (cond-> (get-mock-data @state)
          (:avatar? @state)
          (assoc :avatar community-avatar))]])))
