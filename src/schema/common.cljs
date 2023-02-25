(ns schema.common
  (:require [schema.registry :as registry]))

(defn- ?timestamp
  []
  [:or zero? pos-int?])

(defn- ?public-key
  []
  [:string {:min 1}])

(defn- ?style
  []
  [:map-of :keyword [:or :int :string :keyword]])

(defn- ?theme
  []
  [:enum :light :dark])

(defn- ?icon-name
  []
  [:qualified-keyword {:namespace :i}])

(defn- ?translation
  []
  [:qualified-keyword {:namespace :t}])

(defn- ?color
  []
  [:or
   :string
   [:enum
    :army
    :beige
    :blue
    :brown
    :camel
    :copper
    :danger
    :green
    :indigo
    :magenta
    :orange
    :pink
    :primary
    :purple
    :red
    :sky
    :success
    :turquoise
    :yellow]])

(defn register-schemas
  []
  (registry/def ::color (?color))
  (registry/def ::icon-name (?icon-name))
  (registry/def ::public-key (?public-key))
  (registry/def ::style (?style))
  (registry/def ::theme (?theme))
  (registry/def ::timestamp (?timestamp))
  (registry/def ::translation (?translation)))
