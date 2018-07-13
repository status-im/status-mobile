(ns status-im.extensions.registry
  (:require [pluto.reader :as reader]
            [pluto.registry :as registry]
            [status-im.ui.components.react :as react]))

(def components
  {'view react/view
   'text react/text})

(def extensions
  '{meta {:name          ""
          :description   ""
          :documentation ""}

    views/CK
    [view {}
     [view {:style {:flex 1}}
      [text {}
       "CK"
       #_(or name (i18n/label :t/cryptokitty-name {:id id}))]
      [text {}
       "Short bio"]]]

    hooks/status.collectibles.CK
    {:name     "CryptoKitty"
     :symbol   "CK"
     :view     @views/CK
     :contract ""}})

(defn parse [m]
  (reader/parse {:capacities {:components components
                              :hooks {'hooks/status.collectibles {:properties [{:name :name :type :string}
                                                                               {:name :symbol :type :string}
                                                                               {:name :view :type :view}
                                                                               {:name :contract :type :string}]}}}}
                m))

(def registry (registry/new-registry))

(def id "status")

(let [{:keys [data errors] :as ext} (parse extensions)]
  (when errors
    (throw (ex-info "Failed to parse status extensions" ext)))
  (registry/add! registry id data)
  (registry/activate! registry id))

(defn collectibles []
  (registry/hooks registry 'hooks/status.collectibles))
