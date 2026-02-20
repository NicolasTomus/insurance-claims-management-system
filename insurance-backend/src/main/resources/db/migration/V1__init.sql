--
-- PostgreSQL database dump
--


-- Dumped from database version 16.11 (Debian 16.11-1.pgdg13+1)
-- Dumped by pg_dump version 16.11 (Debian 16.11-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: broker; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.broker (
                               id bigint NOT NULL,
                               broker_code character varying(255),
                               commission_percentage numeric(38,2),
                               email character varying(255),
                               name character varying(255),
                               phone character varying(255),
                               status smallint,
                               CONSTRAINT broker_status_check CHECK (((status >= 0) AND (status <= 1)))
);


ALTER TABLE public.broker OWNER TO insurance_user;

--
-- Name: broker_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.broker_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.broker_seq OWNER TO insurance_user;

--
-- Name: buildings; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.buildings (
                                  id bigint NOT NULL,
                                  address character varying(255) NOT NULL,
                                  building_type character varying(255) NOT NULL,
                                  construction_year integer NOT NULL,
                                  earthquake_risk_zone boolean NOT NULL,
                                  flood_zone boolean NOT NULL,
                                  insured_value numeric(38,2) NOT NULL,
                                  number_of_floors integer NOT NULL,
                                  surface_area numeric(38,2) NOT NULL,
                                  city_id bigint NOT NULL,
                                  owner_id bigint NOT NULL,
                                  CONSTRAINT buildings_building_type_check CHECK (((building_type)::text = ANY ((ARRAY['RESIDENTIAL'::character varying, 'OFFICE'::character varying, 'INDUSTRIAL'::character varying])::text[])))
);


ALTER TABLE public.buildings OWNER TO insurance_user;

--
-- Name: buildings_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.buildings_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.buildings_seq OWNER TO insurance_user;

--
-- Name: cities; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.cities (
                               id bigint NOT NULL,
                               name character varying(255) NOT NULL,
                               county_id bigint NOT NULL
);


ALTER TABLE public.cities OWNER TO insurance_user;

--
-- Name: cities_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.cities_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cities_seq OWNER TO insurance_user;

--
-- Name: clients; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.clients (
                                id bigint NOT NULL,
                                address character varying(255),
                                client_type character varying(255) NOT NULL,
                                email character varying(255) NOT NULL,
                                identification_number character varying(255) NOT NULL,
                                name character varying(255) NOT NULL,
                                phone character varying(255) NOT NULL,
                                CONSTRAINT clients_client_type_check CHECK (((client_type)::text = ANY ((ARRAY['INDIVIDUAL'::character varying, 'COMPANY'::character varying])::text[])))
);


ALTER TABLE public.clients OWNER TO insurance_user;

--
-- Name: clients_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.clients_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.clients_seq OWNER TO insurance_user;

--
-- Name: counties; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.counties (
                                 id bigint NOT NULL,
                                 name character varying(255) NOT NULL,
                                 country_id bigint NOT NULL
);


ALTER TABLE public.counties OWNER TO insurance_user;

--
-- Name: counties_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.counties_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.counties_seq OWNER TO insurance_user;

--
-- Name: countries; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.countries (
                                  id bigint NOT NULL,
                                  name character varying(255) NOT NULL
);


ALTER TABLE public.countries OWNER TO insurance_user;

--
-- Name: countries_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.countries_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.countries_seq OWNER TO insurance_user;

--
-- Name: currencies; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.currencies (
                                   id bigint NOT NULL,
                                   active boolean NOT NULL,
                                   code character varying(255) NOT NULL,
                                   exchange_rate_to_base numeric(38,2) NOT NULL,
                                   name character varying(255) NOT NULL
);


ALTER TABLE public.currencies OWNER TO insurance_user;

--
-- Name: currencies_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.currencies_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.currencies_seq OWNER TO insurance_user;

--
-- Name: fee_configuration; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.fee_configuration (
                                          id bigint NOT NULL,
                                          active boolean NOT NULL,
                                          effective_from date,
                                          effective_to date,
                                          name character varying(255),
                                          percentage numeric(38,2),
                                          type character varying(255),
                                          CONSTRAINT fee_configuration_type_check CHECK (((type)::text = ANY ((ARRAY['BROKER_COMMISSION'::character varying, 'RISK_ADJUSTMENT'::character varying, 'ADMIN_FEE'::character varying])::text[])))
);


ALTER TABLE public.fee_configuration OWNER TO insurance_user;

--
-- Name: fee_configuration_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.fee_configuration_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fee_configuration_seq OWNER TO insurance_user;

--
-- Name: policies; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.policies (
                                 id bigint NOT NULL,
                                 activated_at timestamp(6) without time zone,
                                 base_premium_amount numeric(19,2) NOT NULL,
                                 cancellation_reason character varying(500),
                                 cancelled_at timestamp(6) without time zone,
                                 created_at timestamp(6) without time zone NOT NULL,
                                 end_date date NOT NULL,
                                 final_premium_amount numeric(19,2),
                                 policy_number character varying(255) NOT NULL UNIQUE,
                                 start_date date NOT NULL,
                                 status character varying(255) NOT NULL,
                                 updated_at timestamp(6) without time zone NOT NULL,
                                 broker_id bigint NOT NULL,
                                 building_id bigint NOT NULL,
                                 client_id bigint NOT NULL,
                                 currency_id bigint NOT NULL,
                                 CONSTRAINT policies_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ACTIVE'::character varying, 'EXPIRED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.policies OWNER TO insurance_user;

--
-- Name: policies_id_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

ALTER TABLE public.policies ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.policies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: risk_factor_configurations; Type: TABLE; Schema: public; Owner: insurance_user
--

CREATE TABLE public.risk_factor_configurations (
                                                   id bigint NOT NULL,
                                                   active boolean NOT NULL,
                                                   adjustment_percentage numeric(8,4) NOT NULL,
                                                   level character varying(255) NOT NULL,
                                                   reference_id bigint,
                                                   CONSTRAINT risk_factor_configurations_level_check CHECK (((level)::text = ANY ((ARRAY['COUNTRY'::character varying, 'COUNTY'::character varying, 'CITY'::character varying, 'BUILDING_TYPE'::character varying])::text[])))
);


ALTER TABLE public.risk_factor_configurations OWNER TO insurance_user;

--
-- Name: risk_factor_configurations_seq; Type: SEQUENCE; Schema: public; Owner: insurance_user
--

CREATE SEQUENCE public.risk_factor_configurations_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.risk_factor_configurations_seq OWNER TO insurance_user;

--
-- Name: broker broker_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.broker
    ADD CONSTRAINT broker_pkey PRIMARY KEY (id);


--
-- Name: buildings buildings_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.buildings
    ADD CONSTRAINT buildings_pkey PRIMARY KEY (id);


--
-- Name: cities cities_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT cities_pkey PRIMARY KEY (id);


--
-- Name: clients clients_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (id);


--
-- Name: counties counties_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.counties
    ADD CONSTRAINT counties_pkey PRIMARY KEY (id);


--
-- Name: countries countries_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);


--
-- Name: currencies currencies_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.currencies
    ADD CONSTRAINT currencies_pkey PRIMARY KEY (id);


--
-- Name: fee_configuration fee_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.fee_configuration
    ADD CONSTRAINT fee_configuration_pkey PRIMARY KEY (id);


--
-- Name: policies policies_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT policies_pkey PRIMARY KEY (id);


--
-- Name: risk_factor_configurations risk_factor_configurations_pkey; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.risk_factor_configurations
    ADD CONSTRAINT risk_factor_configurations_pkey PRIMARY KEY (id);


--
-- Name: cities uk_city_county; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT uk_city_county UNIQUE (county_id, name);


--
-- Name: clients uk_client_identifier; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT uk_client_identifier UNIQUE (identification_number);


--
-- Name: countries uk_country_name; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.countries
    ADD CONSTRAINT uk_country_name UNIQUE (name);


--
-- Name: counties uk_county_country; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.counties
    ADD CONSTRAINT uk_county_country UNIQUE (country_id, name);


--
-- Name: policies ukoa74bk3bbln2o1hgik4b93rp9; Type: CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT ukoa74bk3bbln2o1hgik4b93rp9 UNIQUE (policy_number);


--
-- Name: policies fk8vk2roc9qc7tjv1r676uajn1j; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT fk8vk2roc9qc7tjv1r676uajn1j FOREIGN KEY (currency_id) REFERENCES public.currencies(id);


--
-- Name: policies fk9fvidi377wjf8it22kknk7lye; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT fk9fvidi377wjf8it22kknk7lye FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: buildings fk_building_city; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.buildings
    ADD CONSTRAINT fk_building_city FOREIGN KEY (city_id) REFERENCES public.cities(id);


--
-- Name: buildings fk_building_client; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.buildings
    ADD CONSTRAINT fk_building_client FOREIGN KEY (owner_id) REFERENCES public.clients(id);


--
-- Name: cities fk_city_county; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT fk_city_county FOREIGN KEY (county_id) REFERENCES public.counties(id);


--
-- Name: counties fk_county_country; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.counties
    ADD CONSTRAINT fk_county_country FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: policies fkhtqlsvfcxojqmjvo8b5e2hbuh; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT fkhtqlsvfcxojqmjvo8b5e2hbuh FOREIGN KEY (broker_id) REFERENCES public.broker(id);


--
-- Name: policies fkl4vv6y7cg3e80qqalwkakwolj; Type: FK CONSTRAINT; Schema: public; Owner: insurance_user
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT fkl4vv6y7cg3e80qqalwkakwolj FOREIGN KEY (building_id) REFERENCES public.buildings(id);


--
-- PostgreSQL database dump complete
--


