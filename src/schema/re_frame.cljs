(ns schema.re-frame
  (:require
    [clojure.test.check.generators :as gen]
    [schema.registry :as registry]))

(defn ?event
  []
  [:catn
   [:event-id :keyword]
   [:event-args [:* :any]]])

(def rpc-method-generator
  (gen/bind gen/string-alphanumeric
            (fn [s]
              (gen/return (str "wakuext_" s)))))

(def rpc-params-generator
  (gen/vector (gen/one-of [gen/small-integer gen/string-alphanumeric])
              0
              4))

(defn- ?rpc-call
  []
  [:vector {:min 1 :gen/max 1}
   [:map {:closed true}
    [:method [:re {:gen/gen rpc-method-generator} #"^wakuext_.+$"]]
    [:params [:vector {:gen/gen rpc-params-generator} :any]]
    [:on-success [:or ::event [fn? {:gen/return identity}]]]
    [:on-error [:or ::event [fn? {:gen/return identity}]]]]])

(defn ?effects
  []
  [:map
   [:db {:optional true} ::db]
   [:json-rpc/call {:optional true} ::rpc-call]])

(defn ?activity-center
  []
  [:map {:closed true}
   [:filter {:required true}
    [:map
     [:status [:enum :read :unread]]
     [:type [:enum 0 1 2 3 4 5 7 8 9 10]]]]
   [:loading? {:optional true} :boolean]
   [:contact-requests {:optional true} :any]
   [:cursor {:optional true} :string]
   [:notifications {:optional true} [:sequential :schema.shell/notification]]
   [:seen? {:optional true} :boolean]
   [:unread-counts-by-type {:optional true}
    [:map-of {:min 1} :schema.shell/notification-type :int]]])

(defn ?db
  []
  [:map [:activity-center {:optional true} (?activity-center)]])

(defn ?cofx
  []
  [:map
   [:db (?db)]])

(defn register-schemas
  []
  (registry/def ::event (?event))
  (registry/def ::rpc-call (?rpc-call))
  (registry/def ::db (?db))
  (registry/def ::cofx (?cofx))
  (registry/def ::effects (?effects)))
