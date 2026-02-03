# 👟 CREAM
실시간 시세 거래 플랫폼 특징 중 '입찰 시스템'과 '결제 검증'을 구현한 백엔드 프로젝트입니다.

## 1. 프로젝트 소개 (Project Overview)
본 프로젝트는 스니커즈 및 한정판 굿즈의 리셀 거래를 중개하는 'KREAM' 플랫폼의 핵심 비즈니스 로직을 모티브로 하였습니다. 
상품 판매, 실시간 시세에 기반한 구매/판매 입찰(Bidding) 기능과 
거래 신뢰도를 확보하기 위한 외부 결제 API(PortOne) 연동 및 사후 검증 로직을 구현하는 데 초점을 맞췄습니다.

* **핵심 목표**: 
* **주요 성과**: 

## 2. 기술 스택 (Tech Stack)
### Backend
* Language: Java 17
* Framework: Spring Boot 3.5.10
* Database: MySQL
* ORM: Spring Data JPA
* Payment: PortOne API(SDK)

### Tools
* Build: Gradle
* Documentation: Postman
* Version Control: Git

## 3. ERD 및 아키텍처 (ERD & Architecture)
### 엔티티 관계도 (ERD)
[이미지 첨부]

프로젝트의 데이터 모델은 거래의 유연성과 이력 관리에 집중하여 설계되었습니다.
* Bid (Purchase/Sale): 사용자의 입찰 정보를 저장하며, Trade로 체결되기 전까지의 상태를 관리합니다.
* Trade: 입찰이 일치하거나 즉시 구매/판매가 이루어질 때 생성되는 핵심 거래 데이터입니다.
* Payment & History: 결제 상태(READY, PAID, CANCELLED)를 관리하며, 결제 시점의 모든 로그를 PaymentHistory에 기록하여 추적 가능성을 높였습니다.
* BaseTimeEntity: 모든 엔티티가 상속받아 생성/수정 시간을 자동 추적하도록 공통화하였습니다.

### 시스템 아키텍처

## 4. 주요 기능 (Key Features)

## 5. 기술적 의사결정 및 트러블슈팅 (Technical Decision & Troubleshooting)

## 6. API 명세 (API Documentation)

## 7. 회고 및 아쉬운 점 (Retrospective)

## 8. 설치 및 실행 방법 (How to run)
