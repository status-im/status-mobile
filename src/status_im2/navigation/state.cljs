(ns status-im2.navigation.state)

(defonce root-comp-id (atom nil))
(defonce root-id (atom nil))
(defonce pushed-screen-id (atom nil))
(defonce curr-modal (atom nil))
(defonce modals (atom []))
(defonce dissmissing (atom false))
(def shared-element (atom {:id "0xdb5bb2aa7a3711be379fce02c25cd33ec75c34304caafbf06d21d3f15679c1a6"}))
