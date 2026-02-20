openapi: 3.0.0
info:
  title: LTI (Learning Technology Initiative) API
  description: |
    REST API for the Learning Technology Initiative platform, designed for managing candidates, positions, interviews, and recruitment processes.
    
    This API follows Domain-Driven Design principles and implements a layered architecture for scalable recruitment management.
    
    ## Features
    - Candidate management with education and work experience tracking
    - Position management with company and interview flow integration
    - Application tracking through interview processes
    - File upload capabilities for resumes and documents
    - Comprehensive filtering, sorting, and pagination support
    
  version: 0.0.0.001
  contact:
    name: LTI Development Team
  license:
    name: ISC
    
servers:
  - url: http://localhost:3000
    description: Development server
  - url: https://api.lti.dev
    description: Production server

paths:
  