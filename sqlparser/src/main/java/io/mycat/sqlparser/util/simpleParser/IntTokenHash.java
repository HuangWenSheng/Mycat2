package io.mycat.sqlparser.util.simpleParser;

/**
 * Created by Kaiz on 2017/3/4.
 */
public class IntTokenHash {

    //  generate by  DCLSQLParserHelper
//    public final static int ALL = -1650851837;
//    public final static int FILE = -860749820;
    public final static int PROCESS = 1234436103;
//    public final static int PROXY = -180420603;
//    public final static int REFERENCES = -367263734;
//    public final static int EVENT=-992346107;
//    public final static int REPLICATION = 272760843;
//    public final static int SHUTDOWN = 524550152;
//    public final static int SUPER = -1079377915;
//    public final static int TRIGGER = 1295319047;
//    public final static int USAGE = 1964179461;


    public static final int ANNOTATION_BALANCE = 0x00001401;
    public static final int ANNOTATION_START = 0x00001502;
    public static final int ANNOTATION_END = 0x00001602;
    public static final int SQL_DELIMETER = 0x00000c01;

    //generate by MatchMethodGenerator.GenerateSqlTokenHash
    public static final int BETWEEN          = 0x001f0307;
    public static final int AS               = 0xa8465802;
    public static final int ID               = 0xa8476702;
    public static final int IF               = 0xa8476502;
    public static final int ON               = 0xa8409e02;
    public static final int XA               = 0xa8416502;
    public static final int ALL              = 0x9a9d9a03;
    public static final int END              = 0x9b4b1203;
    public static final int FOR              = 0x9b576e03;
    public static final int NOT              = 0x9a68b003;
    public static final int OFF              = 0x9a626003;
    public static final int SET              = 0x9a523303;
    public static final int SQL              = 0x9a538203;
    public static final int USE              = 0x9a0f8403;
    public static final int XML              = 0x9a7fe203;
    public static final int CALL             = 0x6225d004;
    public static final int DATA             = 0x8b30b504;
    public static final int DROP             = 0x8bba5d04;
    public static final int DESC             = 0x8bc08f04;
    public static final int FILE             = 0x8accb204;
    public static final int FROM             = 0x8a992e04;
    public static final int HELP             = 0x64d4e304;
    public static final int INTO             = 0x64b2e704;
    public static final int JOIN             = 0x94f97004;
    public static final int KILL             = 0x665f7f04;
    public static final int LOAD             = 0x6eed8b04;
    public static final int LOCK             = 0x6eed3304;
    public static final int LEFT             = 0x6ef4e504;
    public static final int NAME             = 0x69332004;
    public static final int REPL             = 0x6b320204;
    public static final int SHOW             = 0x6ab03004;
    public static final int SLOW             = 0x6aa33404;
    public static final int TIME             = 0x725f0504;
    public static final int USER             = 0x727bfa04;
    public static final int ALTER            = 0x91b2db05;
    public static final int BEGIN            = 0x37f8f505;
    public static final int CLEAR            = 0x3419bf05;
    public static final int CACHE            = 0x7ff61c05;
    public static final int EVENT            = 0x0ec4da05;
    public static final int GROUP            = 0x3c43f505;
    public static final int GRANT            = 0x3c29c205;
    public static final int INDEX            = 0x0bfcff05;
    public static final int LIMIT            = 0xf2450a05;
    public static final int LOCAL            = 0xd77a2005;
    public static final int MYCAT            = 0xcbdce505;
    public static final int ORDER            = 0x6188da05;
    public static final int PROXY            = 0x9df53f05;
    public static final int QUERY            = 0x8adf8c05;
    public static final int RIGHT            = 0x2bec6105;
    public static final int ROUTE            = 0x3d82ef05;
    public static final int START            = 0x29bf8805;
    public static final int SUPER            = 0x29bfaa05;
    public static final int TABLE            = 0x56037805;
    public static final int UNION            = 0xe0cf9d05;
    public static final int USAGE            = 0x5e751305;
    public static final int WHERE            = 0x0b759505;
    public static final int CATLET           = 0xc0da6006;
    public static final int COMMIT           = 0x57ac8506;
    public static final int CONFIG           = 0x65ebd606;
    public static final int CREATE           = 0x8cdde006;
    public static final int DETAIL           = 0x0c476906;
    public static final int DELETE           = 0x3033e506;
    public static final int EXISTS           = 0xf22ed706;
    public static final int IGNORE           = 0x026c3606;
    public static final int INFILE           = 0x2eba2406;
    public static final int INSERT           = 0x46a3b106;
    public static final int ONLINE           = 0x0f05bc06;
    public static final int RELOAD           = 0xf9d72006;
    public static final int RENAME           = 0xe5e6c606;
    public static final int REVOKE           = 0xe15cc206;
    public static final int ROUTER           = 0x023c5706;
    public static final int SELECT           = 0xf402a106;
    public static final int SERVER           = 0xf831fa06;
    public static final int SCHEMA           = 0x6a0c4906;
    public static final int SWITCH           = 0xdfe05006;
    public static final int UNLOCK           = 0xb659fc06;
    public static final int UPDATE           = 0x92628406;
    public static final int BACKEND          = 0xef9c0b07;
    public static final int BALANCE          = 0x788d0707;
    public static final int CHARSET          = 0x255b1e07;
    public static final int CURRENT          = 0x436af607;
    public static final int DB_TYPE          = 0x0dac6107;
    public static final int DELAYED          = 0x28563807;
    public static final int EXECUTE          = 0x89304b07;
    public static final int EXPLAIN          = 0x67e41f07;
    public static final int OFFLINE          = 0x809ead07;
    public static final int PREPARE          = 0x97cf6507;
    public static final int RECOVER          = 0x97e7dc07;
    public static final int RELEASE          = 0xfa306507;
    public static final int REPLACE          = 0x30a88c07;
    public static final int REPLICA          = 0x30cb9407;
    public static final int SESSION          = 0x80f20807;
    public static final int STARTUP          = 0xdd167707;
    public static final int TRIGGER          = 0x4d4d3507;
    public static final int VERSION          = 0xcf663807;
    public static final int DATABASE         = 0x04bbcc08;
    public static final int DATANODE         = 0xd1045d08;
    public static final int DATANODES = -2050455543;
    public static final int DESCRIBE         = 0x5b8ad308;
    public static final int ROLLBACK         = 0xc0e73908;
    public static final int SHUTDOWN         = 0x201f4408;
    public static final int TRUNCATE         = 0xe8cb5d08;
    public static final int BENCHMARK        = 0xdfc8b809;
    public static final int COLLATION        = 0x481a6e09;
    public static final int HEARTBEAT        = 0x26abb709;
    public static final int PROCEDURE        = 0xd7116009;
    public static final int PROCESSOR        = 0x62fdbc09;
    public static final int REPL_NAME        = 0xe4615d09;
    public static final int ROW_COUNT        = 0x78dfe309;
    public static final int SAVEPOINT        = 0x3bca1609;
    public static final int CACHE_TIME       = 0x7390470a;
    public static final int CONCURRENT       = 0xcc91cf0a;
    public static final int CONNECTION       = 0xb7307d0a;
    public static final int DATASOURCE       = 0xae0b350a;
    public static final int FOUND_ROWS       = 0x86eeb40a;
    public static final int REFERENCES       = 0x11ea1c0a;
    public static final int THREADPOOL       = 0x215a7a0a;
    public static final int REPLICATION      = 0xca10420b;
    public static final int SYSTEM_USER      = 0xc2011e0b;
    public static final int ACCESS_COUNT     = 0x46ec300c;
    public static final int AUTO_REFRESH     = 0x8506b80c;
    public static final int CACHE_RESULT     = 0x45c45b0c;
    public static final int COERCIBILITY     = 0x45247c0c;
    public static final int CURRENT_USER     = 0xbc49710c;
    public static final int LOW_PRIORITY     = 0x35b8050c;
    public static final int SESSION_USER     = 0x071b9b0c;
    public static final int CONNECTION_ID    = 0xf71b850d;
    public static final int HIGH_PRIORITY    = 0xc97c6f0d;
    public static final int LAST_INSERT_ID   = 0x7b07370e;
    public static final int REPL_METABEAN_INDEX = 0x79a7b413;
    public static final int MERGE = -984306427;
    public static final int GROUP_COLUMNS = 1964591628;
    public static final int MERGE_COLUMNS = -1057453300;
    public static final int HAVING = -102177274;
    public static final int LIMIT_START = -1978316790;
    public static final int LIMIT_SIZE = -1728251639;
    public static final int DATABASES =-0x63eca2f7;
    public static final int TABLES =0x43ec8406;

}
